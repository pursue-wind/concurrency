package cn.mirror.concurrency.example.immutable;

import cn.mirror.concurrency.annotations.ThreadSafe;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * @author mirror
 */
@ThreadSafe
public class ImmutableExample3 {
    private final static ImmutableList<Integer> list = ImmutableList.of(1, 2, 3, 4);
    private final static ImmutableSet<Integer> set = ImmutableSet.copyOf(list);
    private final static ImmutableMap<Integer, Integer> map = ImmutableMap.of(1, 1, 2, 2, 3, 3);
    private final static ImmutableMap<Integer, Integer> map2 = ImmutableMap.<Integer, Integer>builder().put(1, 1).put(2, 2).build();

    public static void main(String[] args) {
        //下面的运行会抛出 UnsupportedOperationException 异常
        list.add(1);
        set.add(1);
        map.put(1, 2);
    }
}
