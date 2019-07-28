package cn.mirror.concurrency.example.singleton;

import cn.mirror.concurrency.annotations.ThreadSafe;

/**
 * 饿汉模式
 *
 * @author mirror
 */
@ThreadSafe
public class SingletonExample2 {
    private static SingletonExample2 instance = new SingletonExample2();

    private SingletonExample2() {
    }

    public static SingletonExample2 getInstance() {
        return instance;
    }
}
