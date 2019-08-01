package cn.mirror.concurrency.example.future;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class FutureExample {

    static class MyCallable implements Callable<String> {

        @Override
        public String call() throws Exception {
            log.info("do something in callable!");
            Thread.sleep(3000);
            return "Done";
        }
    }

    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();
        Future<String> future = pool.submit(new MyCallable());
        log.info("do something in main!");
        try {
            String res = future.get();
            log.info("result: {}", res);
        } catch (Exception e) {
            log.error("{}", e);
        } finally {
            pool.shutdown();
        }


    }
}
