package cn.mirror.concurrency.example.syncContainer;

import cn.mirror.concurrency.annotations.NotThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.Vector;
import java.util.concurrent.*;

/**
 * @author mirror
 */
@Slf4j
@NotThreadSafe
public class VectorExample2 {
    private static Vector<Integer> vector = new Vector<>();

    /**
     * 会抛出数组越界异常
     * Exception in thread "Thread-42" Exception in thread "Thread-41" java.lang.ArrayIndexOutOfBoundsException: Array index out of range: 6
     */
    public static void main(String[] args) {
        while (true) {
            for (int i = 0; i < 10; i++) {
                vector.add(i);
            }
            Thread thread1 = new Thread(() -> {
                for (int i = 0; i < vector.size(); i++) {
                    vector.remove(i);
                }
            });

            Thread thread2 = new Thread(() -> {
                for (int i = 0; i < vector.size(); i++) {
                    vector.get(i);
                }
            });
            thread1.start();
            thread2.start();
        }
    }
}
