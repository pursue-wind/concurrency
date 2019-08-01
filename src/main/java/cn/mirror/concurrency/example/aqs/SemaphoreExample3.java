package cn.mirror.concurrency.example.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author mirror
 */
@Slf4j
public class SemaphoreExample3 {
    public static final int threadTotal = 20;

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(3);
        for (int i = 0; i < threadTotal; i++) {
            final int count = i;
            executorService.execute(() -> {
                try {
                    //尝试获取许可，
                    if (semaphore.tryAcquire()) {
                        test(count);
                        semaphore.release(3);    //释放3个许可
                    }
                } catch (Exception e) {
                    log.error("exception: {}", e);
                }
            });
        }
        executorService.shutdown();
    }

    private static void test(int i) throws InterruptedException {
        log.info("{}", i);
        Thread.sleep(1000);
    }
}
