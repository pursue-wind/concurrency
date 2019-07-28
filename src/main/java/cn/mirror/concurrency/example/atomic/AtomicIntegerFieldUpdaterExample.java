package cn.mirror.concurrency.example.atomic;

import cn.mirror.concurrency.annotations.ThreadSafe;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author mirror
 */
@Slf4j
@ThreadSafe
public class AtomicIntegerFieldUpdaterExample {

    private static AtomicIntegerFieldUpdater updater =
            AtomicIntegerFieldUpdater.newUpdater(AtomicIntegerFieldUpdaterExample.class, "count");
    @Getter
    //必须有 volatile ，而且没有 static 修饰
    public volatile int count = 100;

    private static AtomicIntegerFieldUpdaterExample example = new AtomicIntegerFieldUpdaterExample();

    public static void main(String[] args) {
        if (updater.compareAndSet(example, 100, 120)) {
            log.info("update success 1: {}", example.getCount());
        }
        if (updater.compareAndSet(example, 100, 120)) {
            log.info("update success 2: {}", example.getCount());
        } else {
            log.info("update failed: {}", example.getCount());
        }
    }
}
