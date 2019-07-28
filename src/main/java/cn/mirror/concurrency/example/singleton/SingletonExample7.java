package cn.mirror.concurrency.example.singleton;

import cn.mirror.concurrency.annotations.Recommend;
import cn.mirror.concurrency.annotations.ThreadSafe;

/**
 * 枚举模式
 *
 * @author mirror
 */
@ThreadSafe
@Recommend
public class SingletonExample7 {

    private SingletonExample7() {
    }

    public SingletonExample7 getInstance() {
        return Singleton.INSTANCE.getInstance();
    }

    private enum Singleton {
        INSTANCE;
        private SingletonExample7 singleton;

        /**
         * JVM 保证这个方法只被调用一次
         */
        Singleton() {
            singleton = new SingletonExample7();
        }

        public SingletonExample7 getInstance() {
            return singleton;
        }
    }
}