package com.javalabs.exception;

/**
 * 一个业务级别的受检异常 (Checked Exception)
 * 
 * 为什么继承 Exception？
 * 因为“余额不足”是一个高度可能发生的业务错误，
 * 我们希望强迫调用者显式处理它（报错或是提示充值），而不是让程序直接崩溃。
 */
public class InsufficientFundsException extends Exception {
    private final double amountNeeded;

    public InsufficientFundsException(double amountNeeded) {
        super("Insufficient funds! You need " + amountNeeded + " more.");
        this.amountNeeded = amountNeeded;
    }

    public double getAmountNeeded() {
        return amountNeeded;
    }
}
