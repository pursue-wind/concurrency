package cn.mirror.concurrency.example.syncContainer;

import cn.mirror.concurrency.annotations.NotThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Vector;

/**
 * @author mirror
 */
@Slf4j
@NotThreadSafe
public class VectorExample3 {

    /**
     * 会抛出 java.util.ConcurrentModificationException 异常
     */
    private static void test1(Vector<Integer> v1) { // foreach
        for (Integer i : v1) {
            if (i.equals(3)) {
                v1.remove(i);
            }
        }
    }

    /**
     * 会抛出 java.util.ConcurrentModificationException 异常
     */
    private static void test2(Vector<Integer> v1) { // foreach
        Iterator<Integer> iterable = v1.iterator();
        while (iterable.hasNext()) {
            Integer next = iterable.next();
            if (next.equals(3)) {
                v1.remove(3);
            }
        }
    }

    /**
     * 正常运行
     */
    private static void test3(Vector<Integer> v1) { // foreach
        for (int i = 0; i < v1.size(); i++) {
            if (v1.get(i).equals(3)) {
                v1.remove(i);
            }
        }
    }

    public static void main(String[] args) {
        Vector<Integer> vector = new Vector<>();
        int i = 0;
        while (i < 10) {
            vector.add(i++);
        }
        test3(vector);
    }
}
