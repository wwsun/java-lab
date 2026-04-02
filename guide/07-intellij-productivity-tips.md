# 07-IntelliJ IDEA 高效生产力技巧 (IntelliJ Productivity Tips)

作为从 Node.js/TypeScript 转过来的开发者，你可能习惯了 VS Code 的 Snippets。在 IDEA 中，这个功能由 **Live Templates** 和 **Postfix Completion** 组成。

## 1. 快速打印控制台 (System.out.println)

别再手敲 `System.out.println` 了，尝试以下缩写（输入后按 `Tab`）：

- **`sout`**：生成 `System.out.println();`
- **`soutv`**：生成 `System.out.println("变量名 = " + 变量值);` (自动寻找最近定义的变量)
- **`soutp`**：生成 `System.out.println("参数1 = " + p1 + ", 参数2 = " + p2);` (自动打印方法入参)
- **`soutm`**：生成 `System.out.println("当前类名.当前方法名");`
- **`err.sout`**：(后缀补全) 将当前表达式放入 `System.err.println()` 中。

## 2. 快速生成代码块

- **`psvm`** 或 **`main`**：快速生成主函数 `public static void main(String[] args) {}`
- **`fori`**：快速生成标准 `for` 循环。
- **`iter`**：快速生成增强型 `for` 循环 (类比 JS 的 `for...of`)。
- **`ifn`**：快速生成 `if (obj == null)`。
- **`inn`**：快速生成 `if (obj != null)`。

## 3. 后缀补全 (Postfix Completion)

在变量名后面直接打点输入，这是 IDEA 的特色功能：

- `myList.for` -> `for (Object o : myList) {}`
- `user == null.if` -> `if (user == null) {}`
- `result.return` -> `return result;`
- `new Object().var` -> `Object object = new Object();` (自动补全左侧变量声明)

## 4. 万能搜索与重构

- **`Shift` + `Shift`**：Search Everywhere (搜索类、文件、设置、符号)。
- **`Cmd` + `O`**：搜索类 (Class)。
- **`Cmd` + `Shift` + `O`**：搜索文件 (File)。
- **`Shift` + `F6`**：重构重命名 (Rename)。它会同步修改所有引用的位置，比 VS Code 的实现更健壮。

---
**自定义路径**：`Settings` -> `Editor` -> `Live Templates`
