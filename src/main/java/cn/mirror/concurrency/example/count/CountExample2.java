package cn.mirror.concurrency.example.count;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class CountExample2 {
    //请求总数
    public static final int clientTotal = 50000;
    //并发总线程数
    public static final int threadTotal = 2000;

    public static volatile int count = 0;

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(clientTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(threadTotal);
        for (int i = 0; i < clientTotal; i++) {
            CompletableFuture.runAsync(() -> {
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
        count++;
    }
}