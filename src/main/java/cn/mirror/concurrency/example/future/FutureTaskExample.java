package cn.mirror.concurrency.example.future;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class FutureTaskExample {

    public static void main(String[] args) throws Exception {
        FutureTask<String> task = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                log.info("do something in callable!");
                Thread.sleep(3000);
                return "Done";
            }
        });

        new Thread(task).start();
        log.info("do something in main!");
        Thread.sleep(2000);
        String res = task.get();
        log.info("result: {}", res);
    }
}
