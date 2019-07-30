package cn.mirror.concurrency.example.syncContainer;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.*;

@Slf4j
public class HashTableExample {
    //请求总数
    public static final int clientTotal = 5000;
    //并发总线程数
    public static final int threadTotal = 2000;

    public static Hashtable<Integer, Integer> hashtable = new Hashtable<>();

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        for (int i = 0; i < clientTotal; i++) {
            final int count = i;
            CompletableFuture.runAsync(() -> {
                try {
                    semaphore.acquire();
                    test(count);
                    semaphore.release();
                } catch (InterruptedException e) {
                    log.error("exception: {}", e);
                }
                countDownLatch.countDown();
            }, executorService);
        }
        countDownLatch.await();
        executorService.shutdown();
        log.info("list: {}", hashtable.size());
    }

    private static void test(int i) {
        hashtable.put(i, i);
    }
}





