package com.javalabs.basics;

import java.util.ArrayList;
import java.util.List;

/**
 * 演示 Java 泛型与类型擦除 (Type Erasure) 及其心智映射。
 *
 * 作为资深 Node.js 开发者，你会发现 Java 泛型在语法层面与 TypeScript 非常相似，
 * 但在其运行时的表现上有显著差异。
 */
public class GenericsDemo {

    /**
     * 定义一个泛型盒子类，类比 TS 的 interface Box<T> { content: T }
     */
    public static class Box<T> {
        private T content;

        public Box(T content) {
            this.content = content;
        }

        public T getContent() {
            return content;
        }

        public void setContent(T content) {
            this.content = content;
        }
    }

    /**
     * 演示类型擦除带来的限制。
     * 在运行时，Java 并不知道 T 的具体类型，它会被擦除为 Object。
     */
    public static <T> String getTypeDescriptor(T item) {
        // ERROR: if (item instanceof T) { ... }  // 编译报错！无法使用泛型类型做运行时判定
        
        // 正确做法：只能看运行时的实际对象类型
        return "Runtime class is: " + item.getClass().getName();
    }

    /**
     * 演示 PECS 规则 (Producer Extends, Consumer Super)
     */
    public static void processNumbers(List<? extends Number> numbers) {
        // numbers 是生产者 (Producer)，我们从中读数据
        for (Number n : numbers) {
            System.out.println("Reading number: " + n);
        }
        // numbers.add(10); // ERROR: 无法写入具体数值，因为编译器不知道 numbers 的确切类型
    }

    public static void addIntegers(List<? super Integer> integers) {
        // integers 是消费者 (Consumer)，我们向其中写数据
        integers.add(100);
        integers.add(200);
        // Number n = integers.get(0); // ERROR: 无法读取具体类型，只能读出 Object，因为它是 Integer 的父类
    }

    public static void main(String[] args) {
        // 1. 基本用法 (Diamond Operator <>)
        Box<String> stringBox = new Box<>("Hello Java");
        System.out.println("Box contains: " + stringBox.getContent());

        // 2. 类型擦除验证
        System.out.println(getTypeDescriptor(123));          // Integer
        System.out.println(getTypeDescriptor("Demo"));       // String

        // 3. 通配符实操
        List<Integer> intList = new ArrayList<>();
        intList.add(1);
        intList.add(2);

        processNumbers(intList); // 允许，因为 Integer 是 Number 的子类
        addIntegers(intList);    // 允许，因为 List<Integer> 是 ? super Integer 的下界
        
        System.out.println("Modified list: " + intList);
    }
}
