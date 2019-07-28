package cn.mirror.concurrency.example.sync;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author mirror
 */
@Slf4j
public class SynchronizedExample {

    public void syncBlock() {
        synchronized (this) {
            for (int i = 0; i < 100; i++) {
                log.info("syncBlock: {}", i);
            }
        }
    }

    public synchronized void syncMethod() {
        for (int i = 0; i < 100; i++) {
            log.info("syncMethod: {}", i);
        }
    }

    public static void main(String[] args) {
        SynchronizedExample example = new SynchronizedExample();
        ExecutorService pool = Executors.newCachedThreadPool();

        CompletableFuture.runAsync(() -> {
            example.syncBlock();
        }, pool);
        CompletableFuture.runAsync(() -> {
            example.syncBlock();
        }, pool);

        pool.shutdown();
    }
}
