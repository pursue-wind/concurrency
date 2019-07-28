package cn.mirror.concurrency.example.immutable;

import cn.mirror.concurrency.annotations.NotThreadSafe;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;

/**
 * @author mirror
 */
@NotThreadSafe
public class ImmutableExample1 {
    private final static Integer a = 1;
    private final static String b = "b";
    private final static Map<Integer, Integer> map = Maps.newHashMap();

    static {
        map.put(1, 2);
        map.put(2, 3);
        map.put(3, 4);
    }

    public static void main(String[] args) {
        map.put(1, 5);
    }

    private void test(final int a) {
//        a = 1;
    }


}
