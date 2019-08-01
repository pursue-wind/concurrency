package cn.mirror.concurrency.example.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CountDownLatchExample2 {
    public static final int threadTotal = 200;

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final CountDownLatch countDownLatch = new CountDownLatch(threadTotal);
        for (int i = 0; i < threadTotal; i++) {
            final int count = i;
            executorService.execute(() -> {
                try {
                    test(count);
                } catch (Exception e) {
                    log.error("exception: {}", e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await(100, TimeUnit.MILLISECONDS);
        executorService.shutdown();//会让当前已有的线程执行完再关闭
        log.info("finish");
    }

    private static void test(int i) throws InterruptedException {
        Thread.sleep(100);
        log.info("{}", i);
        Thread.sleep(100);
    }
}
