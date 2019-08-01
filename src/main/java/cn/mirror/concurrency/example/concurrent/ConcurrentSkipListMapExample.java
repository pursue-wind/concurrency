package cn.mirror.concurrency.example.concurrent;

import cn.mirror.concurrency.annotations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author mirror
 */
@Slf4j
@ThreadSafe
public class ConcurrentSkipListMapExample {

    private final static int CLIENT_TOTAL = 10000000;
    private final static int THREAD_TOTAL = 5000000;
    private static final Map<Integer, Integer> MAP = new ConcurrentSkipListMap<>();

    public static void main(String[] args) throws InterruptedException {
        final long start = System.currentTimeMillis();
        ExecutorService pool = Executors.newCachedThreadPool();
        final CountDownLatch countDownLatch = new CountDownLatch(CLIENT_TOTAL);
        final Semaphore semaphore = new Semaphore(THREAD_TOTAL);

        for (int i = 0; i < CLIENT_TOTAL; i++) {
            final int count = i;
            CompletableFuture.runAsync(() -> {
                try {
                    semaphore.acquire();
                    update(count);
                    semaphore.release();
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }, pool);
            countDownLatch.countDown();
        }
        countDownLatch.await();
        pool.shutdown();
        final long end = System.currentTimeMillis();
        log.info("ConcurrentSkipListMap - SIZE：{} - - {}", MAP.size(), (end - start));
    }

    private static void update(int i) {
        MAP.put(i, i);
    }
}
