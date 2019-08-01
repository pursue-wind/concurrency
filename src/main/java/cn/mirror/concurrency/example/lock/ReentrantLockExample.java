package cn.mirror.concurrency.example.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author mirror
 */
@Slf4j
public class ReentrantLockExample {
    //请求总数
    public static final int clientTotal = 50000;
    //并发总线程数
    public static final int threadTotal = 2000;

    public static int count = 0;

    private final static Lock LOCK = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newCachedThreadPool();
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        final Semaphore semaphore = new Semaphore(threadTotal);
        for (int i = 0; i < clientTotal; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    add();
                    semaphore.release();
                } catch (InterruptedException e) {
                    log.error("exception: {}", e);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        log.info("count: {}, time: {}", count, System.currentTimeMillis() - start);
    }

    private static void add() {
        try {
            LOCK.lock();
            count++;
        } finally {
            LOCK.unlock();
        }
    }
}
