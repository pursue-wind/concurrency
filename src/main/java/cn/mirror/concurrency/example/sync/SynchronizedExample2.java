package cn.mirror.concurrency.example.sync;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author mirror
 */
@Slf4j
public class SynchronizedExample2 {

    public static void syncBlock() {
        synchronized (SynchronizedExample.class) {
            for (int i = 0; i < 100; i++) {
                log.info("syncBlock: {}", i);
            }
        }
    }

    public static synchronized void syncMethod() {
        for (int i = 0; i < 100; i++) {
            log.info("syncMethod: {}", i);
        }
    }

    public static void main(String[] args) {
        SynchronizedExample2 example = new SynchronizedExample2();
        ExecutorService pool = Executors.newCachedThreadPool();

        CompletableFuture.runAsync(() -> {
            syncBlock();
        }, pool);
        CompletableFuture.runAsync(() -> {
            syncBlock();
        }, pool);

        pool.shutdown();
    }
}
