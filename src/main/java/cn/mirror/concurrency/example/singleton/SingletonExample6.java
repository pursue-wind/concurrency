package cn.mirror.concurrency.example.singleton;

import cn.mirror.concurrency.annotations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

/**
 * 饿汉模式
 *
 * @author mirror
 */
@Slf4j
@ThreadSafe
public class SingletonExample6 {
    private static SingletonExample6 instance = null;

    static {
        instance = new SingletonExample6();
    }

    private SingletonExample6() {
    }

    public static SingletonExample6 getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        log.info(String.valueOf(getInstance().hashCode()));
        log.info(String.valueOf(getInstance().hashCode()));
    }
}
