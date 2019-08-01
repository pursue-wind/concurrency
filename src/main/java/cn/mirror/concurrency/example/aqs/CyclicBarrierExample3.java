package cn.mirror.concurrency.example.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author mirror
 */
@Slf4j
public class CyclicBarrierExample3 {

    private static final CyclicBarrier BARRIER = new CyclicBarrier(5, () -> {
        log.info("call back is running...");
    });

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i = 0; i < 20; i++) {
            Thread.sleep(1000);
            final int threadNum = i;
            executor.execute(() -> {
                try {
                    race(threadNum);
                } catch (Exception e) {
                    log.error("{}", e);
                }
            });
        }
    }

    private static void race(int threadNum) throws Exception {
        Thread.sleep(1000);
        log.info(" - {} is ready", threadNum);
        BARRIER.await();
        log.info(" - {} is continue", threadNum);
    }
}
