package cn.mirror.concurrency.example.singleton;

import cn.mirror.concurrency.annotations.NotThreadSafe;

/**
 * 懒汉模式
 *
 * @author mirror
 */
@NotThreadSafe
public class SingletonExample4 {
    private volatile static SingletonExample4 instance = null;

    private SingletonExample4() {
    }

    public static SingletonExample4 getInstance() {
        if (instance == null) {
            synchronized (SingletonExample4.class) {
                if (instance == null) {
                    return instance = new SingletonExample4();
                }
            }
        }
        return instance;
    }
}