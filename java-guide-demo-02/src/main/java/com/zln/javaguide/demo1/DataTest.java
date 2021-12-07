package com.zln.javaguide.demo1;

import lombok.Getter;


public class DataTest {
    @Getter
    private static int counter = 0;
    private static Object locker = new Object();

    public static int reset() {
        counter = 0;
        return counter;
    }
    public synchronized void wrong() {
        synchronized (locker) {
            counter++;
        }
    }
}