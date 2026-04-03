# Java 10 局部变量类型推断（var）指南

> 原文：https://www.baeldung.com/java-10-local-variable-type-inference  
> 作者：Ganesh Pagade  
> 审校：Grzegorz Piwowarek  
> 最后更新：2024年1月16日

---

## 1. 概述

**JDK 10 中最显著的特性之一就是对带有初始值的局部变量进行类型推断。**

本教程将详细介绍这一特性并提供示例。

---

## 2. 简介

在 Java 9 之前，我们必须显式声明局部变量的类型，并确保它与用于初始化该变量的初始值兼容：

```java
String message = "Good bye, Java 9";
```

在 Java 10 中，我们可以这样声明局部变量：

```java
@Test
public void whenVarInitWithString_thenGetStringTypeVar() {
    var message = "Hello, Java 10";
    assertTrue(message instanceof String);
}
```

**我们不需要提供 `message` 的数据类型。** 相反，我们将 `message` 标记为 `var`，编译器会根据右侧初始值的类型推断出 `message` 的类型。

在上面的例子中，`message` 的类型将是 `String`。

**注意，此特性仅适用于带有初始值的局部变量。** 它不能用于成员变量、方法参数、返回类型等——初始值是必需的，因为没有它编译器将无法推断类型。

这项增强有助于减少样板代码；例如：

```java
Map<Integer, String> map = new HashMap<>();
```

现在可以重写为：

```java
var idToNameMap = new HashMap<Integer, String>();
```

这也有助于将注意力集中在变量名上，而不是变量类型上。

另一件需要注意的事情是 **`var` 不是关键字**——这确保了使用 `var` 作为函数或变量名的程序的向后兼容性。`var` 是一个保留类型名，就像 `int` 一样。

最后，请注意 **使用 `var` 没有运行时开销，也不会使 Java 成为动态类型语言。** 变量的类型仍然在编译时推断，之后不能更改。

---

## 3. var 的非法使用

如前所述，`var` 在没有初始值的情况下不起作用：

```java
var n; // ❌ 错误：不能在没有初始值的情况下使用 'var'
```

**✅ 正确写法：**
```java
int n; // 先声明，稍后赋值
// 或者
var n = 0; // 使用 var 必须立即初始化
```

---

用 `null` 初始化也不行：

```java
var emptyList = null; // ❌ 错误：变量初始值为 'null'
```

**✅ 正确写法：**
```java
List<String> emptyList = null; // 显式声明类型
// 或者
var emptyList = new ArrayList<String>(); // 使用具体实例初始化
// 或者
var emptyList = Collections.<String>emptyList(); // 使用工厂方法
```

---

它不能用于非局部变量：

```java
public var = "hello"; // ❌ 错误：此处不允许使用 'var'
```

**✅ 正确写法：**
```java
public String message = "hello"; // 成员变量必须显式声明类型
// 或者在方法内使用 var
public void someMethod() {
    var localMessage = "hello"; // 局部变量可以使用 var
}
```

---

Lambda 表达式需要显式目标类型，因此不能使用 `var`：

```java
var p = (String s) -> s.length() > 10; // ❌ 错误：lambda 表达式需要显式目标类型
```

**✅ 正确写法：**
```java
Predicate<String> p = (String s) -> s.length() > 10; // 显式声明函数式接口类型
// 或者
Predicate<String> p = s -> s.length() > 10; // 参数类型可以省略（由接口推断）
// 或者（Java 11+，lambda 参数可以用 var）
Predicate<String> p = (var s) -> s.length() > 10; // 注意：这里 var 用在 lambda 参数上，而非变量声明
```

---

数组初始值设定项也是如此：

```java
var arr = { 1, 2, 3 }; // ❌ 错误：数组初始值设定项需要显式目标类型
```

**✅ 正确写法：**
```java
int[] arr = { 1, 2, 3 }; // 显式声明数组类型
// 或者
var arr = new int[] { 1, 2, 3 }; // 使用 new 明确数组类型
```

---

## 4. 使用 var 的指南

在某些情况下，`var` 可以合法使用，但可能不是一个好主意。

例如，在代码可读性可能降低的情况下：

```java
var result = obj.process();
```

在这里，虽然使用 `var` 是合法的，但很难理解 `process()` 返回的类型，使代码可读性降低。

[java.net](http://openjdk.java.net/) 有一篇关于 [Java 局部变量类型推断风格指南](http://openjdk.java.net/projects/amber/guides/lvti-style-guide) 的专门文章，讨论了我们在使用此特性时应该如何判断。

另一个最好避免使用 `var` 的情况是在具有长管道的流中：

```java
var x = emp.getProjects.stream()
    .findFirst()
    .map(String::length)
    .orElse(0);
```

使用 `var` 也可能产生意外的结果。

例如，如果我们将它与 Java 7 中引入的菱形运算符一起使用：

```java
var empList = new ArrayList<>();
```

`empList` 的类型将是 `ArrayList<Object>` 而不是 `List<Object>`。如果我们希望它是 `ArrayList<Employee>`，我们必须显式指定：

```java
var empList = new ArrayList<Employee>();
```

**将 `var` 与不可表示类型（non-denotable types）一起使用可能会导致意外错误。**

例如，如果我们将 `var` 与匿名类实例一起使用：

```java
@Test
public void whenVarInitWithAnonymous_thenGetAnonymousType() {
    var obj = new Object() {};
    assertFalse(obj.getClass().equals(Object.class));
}
```

现在，如果我们尝试将另一个 `Object` 赋值给 `obj`，我们会得到编译错误：

```java
obj = new Object(); // 错误：Object 无法转换为 <anonymous Object>
```

这是因为推断出的 `obj` 类型不是 `Object`。

---

## 5. 结论

在本文中，我们通过示例了解了 Java 10 的新局部变量类型推断特性。

本文的配套代码可在 GitHub 上获取。

---

## 关键要点总结

| 要点 | 说明 |
|------|------|
| **适用场景** | 仅适用于带有初始值的局部变量 |
| **不适用场景** | 成员变量、方法参数、返回类型、lambda、数组初始值设定项 |
| **var 不是关键字** | 是保留类型名，向后兼容 |
| **无运行时开销** | 类型在编译时推断，Java 仍是静态类型语言 |
| **可读性考虑** | 当返回类型不明确时避免使用 |
| **菱形运算符陷阱** | `var list = new ArrayList<>()` 推断为 `ArrayList<Object>` |
| **匿名类陷阱** | 推断为匿名类型，而非父类 |
