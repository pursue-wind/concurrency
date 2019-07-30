package cn.mirror.concurrency.example.commonUnsafe;

import cn.mirror.concurrency.annotations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author mirror
 */
@Slf4j
@ThreadSafe
public class JodaTimeExample {

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMdd");
    //请求总数
    public static final int clientTotal = 5000;
    //并发总线程数
    public static final int threadTotal = 2000;


    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(clientTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(threadTotal);
        for (int i = 0; i < clientTotal; i++) {
            final int count = i;
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    test(count);
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

    private static void test(int count) {
        Date date = DateTime.parse("20180808", dateTimeFormatter).toDate();
        log.info("{} - {}", date, count);
    }
}
