package cn.mirror.concurrency.example.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author mirror
 */
@Slf4j
public class SemaphoreExample {
    public static final int threadTotal = 20;

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(2);
        for (int i = 0; i < threadTotal; i++) {
            final int count = i;
            executorService.execute(() -> {
                try {
                    semaphore.acquire();    //获得一个许可
                    test(count);
                    semaphore.release();    //释放一个许可
                } catch (Exception e) {
                    log.error("exception: {}", e);
                }
            });
        }
        executorService.shutdown();
        log.info("finish");
    }

    private static void test(int i) throws InterruptedException {
        log.info("{}", i);
        Thread.sleep(1000);
    }
}
