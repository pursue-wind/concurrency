package cn.mirror.concurrency.example.commonUnsafe;

import cn.mirror.concurrency.annotations.NotThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author mirror
 */
@Slf4j
@NotThreadSafe
public class SimpleDateFormatExample {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    //请求总数
    public static final int clientTotal = 5000;
    //并发总线程数
    public static final int threadTotal = 2000;


    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(clientTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(threadTotal);
        for (int i = 0; i < clientTotal; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    test();
                    semaphore.release();
                } catch (InterruptedException e) {
                    log.error("exception: {}", e);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        executorService.shutdown();
    }

    private static void test() {
        try {
            simpleDateFormat.parse("20180808");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
