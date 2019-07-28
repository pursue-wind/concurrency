package cn.mirror.concurrency.example.singleton;

import cn.mirror.concurrency.annotations.NotThreadSafe;

/**
 * 懒汉模式
 *
 * @author mirror
 */
@NotThreadSafe
public class SingletonExample1 {
    private static SingletonExample1 instance = null;

    private SingletonExample1() {
    }

    public static SingletonExample1 getInstance() {
        if (instance == null) {
            return instance = new SingletonExample1();
        }
        return instance;
    }
}
