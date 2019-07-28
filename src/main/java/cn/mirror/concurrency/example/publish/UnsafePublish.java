package cn.mirror.concurrency.example.publish;

import cn.mirror.concurrency.annotations.NotThreadSafe;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@NotThreadSafe
public class UnsafePublish {
    @Getter
    private String[] numbers = {"1", "2", "3"};

    public static void main(String[] args) {
        UnsafePublish unsafePublish = new UnsafePublish();
        log.info("{}", Arrays.toString(unsafePublish.getNumbers()));

        unsafePublish.getNumbers()[0] = "4";
        log.info("{}", Arrays.toString(unsafePublish.getNumbers()));
    }
}
