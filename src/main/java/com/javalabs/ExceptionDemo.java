package com.javalabs;

/**
 * 演示三种场景：
 * 1. 业务受检异常 (InsufficientFundsException)
 * 2. 运行时非受检异常 (IllegalArgumentException)
 * 3. 错误捕获 (ArithmeticException)
 */
public class ExceptionDemo {

    /**
     * 转账业务：模拟余额扣减
     * 演示如何声明受检异常：必须在方法名后加 throws
     */
    public void transfer(double balance, double amount) throws InsufficientFundsException {
        // 1. Unchecked: 参数不合法，抛出运行时异常 (类比 Node.js Error)
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive!");
        }

        // 2. Checked: 业务异常，编译器强制检查
        if (amount > balance) {
            throw new InsufficientFundsException(amount - balance);
        }

        System.out.println("Transfer successful: " + amount);
    }

    /**
     * 演示运行时非受检异常的处理 (ArithmeticException)
     */
    public int divide(int a, int b) {
        try {
            return a / b;
        } catch (ArithmeticException e) {
            System.err.println("Divide by zero! " + e.getMessage());
            // 通常在此处日志记录后继续向上抛出，或者返回一个默认值
            return 0;
        } finally {
            // 类比 JS: 不管算没算成，资源一定要释放 (如：关闭文件、网络连接)
            System.out.println("Processing division...");
        }
    }

    public static void main(String[] args) {
        ExceptionDemo demo = new ExceptionDemo();
        
        // 演示 1: 捕获受检异常
        try {
            demo.transfer(100.0, 150.0);
        } catch (InsufficientFundsException e) {
            System.err.println("Business Error: " + e.getMessage());
            System.err.println("Amount needed: " + e.getAmountNeeded());
        }

        // 演示 2: 演示非受检异常
        try {
            demo.transfer(100.0, -10.0);
        } catch (InsufficientFundsException e) {
            // 按照契约，受检异常必须被处理
            System.err.println("This won't happen here, but compiler demands a catch: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Runtime Error (Arguments): " + e.getMessage());
        }
    }
}
