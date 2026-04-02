# 03-stream-api-mapping

作为深谙 Node.js 开发的前端/全栈程序员，处理数组数据简直是家常便饭，`filter`, `map`, `reduce` 这三板斧几乎构成了我们数据转换的大半江山。而在 Java 世界，完全对标这一特性的便是 **Stream API**（流式处理，自 Java 8 引入并在后续版本不断增强完善）。

## 核心心智映射

| Node.js `Array.prototype` | Java `java.util.stream.Stream` | 核心差异 |
| --- | --- | --- |
| `Array.filter(fn)` | `stream().filter(Predicate)` | Java 强类型，需要定义好具体的过滤条件类型 |
| `Array.map(fn)` | `stream().map(Function)` | TS 中隐式推断类型，Java 中有特定的 `mapToInt`, `mapToDouble` 免去装箱操作 |
| `Array.reduce(fn, init)` | `stream().reduce(init, BinaryOperator)` | Java 中最常用的是配合 `Collectors` 做规约，而不是直接写底层的 reduce 累加逻辑 |

> **关键认知差异**：
> 在 JS 中，每次调用 `.map()` 或 `.filter()` 都会在内存中**立刻生成一个新的完整数组**。  
> 在 Java 的 Stream 中，所有的中间操作（如 `filter`, `map`）都是**惰性的 (Lazy)**。只有当你最终调用了**终端操作**（Terminal Operation，例如 `.collect(Collectors.toList())` 或 `.reduce()`）时，整个流才会像流水线一样一次性开始处理所有数据。这也是 Java 在处理超级大数据集合时的性能优势所在。

## 场景实战对比

假设我们有一个员工数据集合，需要筛选出所有**在职员工**，获取他们的**薪水**，并计算出**年薪总支出**。

### Node.js / TypeScript 的经典写法

```typescript
type Employee = { name: string; isActive: boolean; salary: number };

const employees: Employee[] = [
  { name: 'Alice', isActive: true, salary: 5000 },
  { name: 'Bob', isActive: false, salary: 6000 },
  { name: 'Charlie', isActive: true, salary: 7000 }
];

const totalSalary = employees
  .filter(emp => emp.isActive)
  .map(emp => emp.salary)
  .reduce((acc, current) => acc + current, 0);

console.log(totalSalary); // 输出: 12000
```

### Java 的 Stream API 等价写法

在 Java 中，我们需要借助 `Stream` 并利用 Java 的强类型与方法引用（Method Reference，类似 `Class::method` 的语法糖）。

```java
import java.util.List;

// 1. 利用 Java 的 Record 优雅定义数据结构 (完全等价于上面的 TS Type)
record Employee(String name, boolean isActive, int salary) {}

public class StreamDemo {
    public static void main(String[] args) {
        // 2. 初始化集合
        var employees = List.of(
            new Employee("Alice", true, 5000),
            new Employee("Bob", false, 6000),
            new Employee("Charlie", true, 7000)
        );

        // 3. 流式处理！
        int totalSalary = employees.stream()
            .filter(Employee::isActive)           // 等同于 emp -> emp.isActive()
            .mapToInt(Employee::salary)           // 等同于 emp -> emp.salary()，且直接转换为无装箱的原始 int 流
            .sum();                               // Java 贴心地提供了默认的 sum() 替代了手写 reduce 累加
            
        System.out.println(totalSalary); // 输出: 12000
    }
}
```

## 进阶：如何将其转为集合？

在处理完流之后，我们通常不止是为了计算一个值，而是需要把转换后的对象重新塞在一个列表里（类比 JS 中 `map` 返回的新数组）。
这时我们需要用到 Java 强大的 `Collectors` 工具类。

```java
// 提取所有活跃员工的姓名列表
List<String> activeNames = employees.stream()
    .filter(Employee::isActive)
    .map(Employee::name)
    .toList(); // Java 16+ 提供的快捷收集方法，替代旧版的 .collect(Collectors.toList())

// 输出: [Alice, Charlie]
```

记住，遇到任何复杂的数据集合转换需求，直接扔给 AI Agent ，“把它翻译成 Java 21 Stream API”，你就能用 Node.js 的思维，写出极其优雅并自带性能 Buff 的现代 Java 业务代码！
