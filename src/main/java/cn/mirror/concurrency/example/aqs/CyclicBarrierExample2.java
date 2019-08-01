package cn.mirror.concurrency.example.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class CyclicBarrierExample2 {

    private static final CyclicBarrier BARRIER = new CyclicBarrier(5);

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
        executor.shutdown();
    }

    private static void race(int threadNum) throws Exception {
        Thread.sleep(1000);
        log.info(" - {} is ready", threadNum);
        try {
            BARRIER.await(2, TimeUnit.SECONDS);
        } catch (BrokenBarrierException | TimeoutException e) {
            log.warn("BrokenBarrierException | TimeoutException ：{}", e);
        }
        log.info(" - {} is continue", threadNum);

    }
}
