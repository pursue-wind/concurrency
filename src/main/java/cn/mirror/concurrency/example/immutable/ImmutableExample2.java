package cn.mirror.concurrency.example.immutable;

import cn.mirror.concurrency.annotations.ThreadSafe;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;

/**
 * @author mirror
 */
@ThreadSafe
public class ImmutableExample2 {
    private static Map<Integer, Integer> map = Maps.newHashMap();

    static {
        map.put(1, 2);
        map.put(2, 3);
        map.put(3, 4);
        map = Collections.unmodifiableMap(map);
    }

    public static void main(String[] args) {
        //运行会抛出 UnsupportedOperationException 异常
        map.put(1, 5);
    }

    private void test(final int a) {
//        a = 1;
    }


}
