package cn.mirror.concurrency.example.singleton;

import cn.mirror.concurrency.annotations.NotRecommend;
import cn.mirror.concurrency.annotations.ThreadSafe;

/**
 * 懒汉模式
 *
 * @author mirror
 */
@ThreadSafe
@NotRecommend
public class SingletonExample3 {
    private static SingletonExample3 instance = null;

    private SingletonExample3() {
    }

    public synchronized static SingletonExample3 getInstance() {
        if (instance == null) {
            return instance = new SingletonExample3();
        }
        return instance;
    }
}
