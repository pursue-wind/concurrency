> 以AtomicInteger的 `incrementAndGet()` 为例

```java
    public final int incrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
    }
```

> getAndAddInt 方法
```java
/**
 * 比如此时执行的是 2+1 操作
 * @param Object var1 AtomicInteger对象
 * @param long   var2 值2 当前值
 * @param int    var4 值1 增加量
 * @return
 */
public final int getAndAddInt(Object var1, long var2, int var4) {
    int var5; //调用底层方法 getIntVolatile 得到的值
    do {
        var5 = this.getIntVolatile(var1, var2);
    } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));
    // 这个方法，当 var2 当前值，和底层值 var5 相同时，就更新为 var5 + var4
    // compareAndSwap 即 CAS
    return var5;
}
```

> compareAndSwapInt方法
```java
public final native boolean compareAndSwapInt(Object var1, long var2, int var4, int var5);
```

