## 线程安全性
### 原子性 - Atomic 包

#### AtomicInteger

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

#### AtomicIntegerFieldUpdater

```java
@Slf4j
@ThreadSafe
public class AtomicIntegerFieldUpdaterExample {

    private static AtomicIntegerFieldUpdater updater =
            AtomicIntegerFieldUpdater.newUpdater(AtomicIntegerFieldUpdaterExample.class, "count");
    @Getter
    //必须有 volatile ，而且没有 static 修饰
    public volatile int count = 100;

    private static AtomicIntegerFieldUpdaterExample example = new AtomicIntegerFieldUpdaterExample();

    public static void main(String[] args) {
        if (updater.compareAndSet(example, 100, 120)) {
            log.info("update success 1: {}", example.getCount());
        }
        if (updater.compareAndSet(example, 100, 120)) {
            log.info("update success 2: {}", example.getCount());
        } else {
            log.info("update failed: {}", example.getCount());
        }
    }
}
```

#### AtomicStampedReference 解决CAS的ABA问题

```java
    public boolean compareAndSet(V   expectedReference,
                                 V   newReference,
                                 int expectedStamp,
                                 int newStamp) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            expectedStamp == current.stamp &&
            ((newReference == current.reference &&
              newStamp == current.stamp) ||
             casPair(current, Pair.of(newReference, newStamp)));
    }
```

#### 原子性 - 对比

- Synchronized：不可中断锁，适合竞争不激烈，可读性好
- Lock：可中断锁，多样化同步，竞争激烈时能维持常态
- Atomic：竞争激烈时能维持常态，比Lock性能好；只能同步一个值



### 线程可见性

#### 导致共享变量在线程间不可见的原因

- 线程交叉执行
- 重排序结合线程交叉执行
- 共享变量更新后的值没有在工作内存和主存间及时更新

#### 可见性 - synchronized

##### JVM关于synchronized的两条规定

- 线程解锁前，必须把共享变量的最新值刷新到主内存
- 线程加锁时，将清空工作内存中共享变量的值，从而使用共享变量时需要从主内存中重新读取最新的值（注意，加锁与解锁是同一把锁）

#### 可见性 - volatile

##### 通过加入内存屏障和禁止重排序来实现

- 对volatile变量进行写操作时，会在写操作后加入一条store屏障指令，将本地内存中的共享变量值刷新到主内存
- 对volatile变量进行读操作时，会在读操作前加入一条load屏障指令，从主内存中读取共享变量

> 变量声明为volatile不能保证线程安全
>
> count++ 实际操作为
>
> 1. 从内存中取出count值
> 2. +1
> 3. 写回主存
>
> 当多个线程同时写回主存时，会丢失+1操作

 

#### 可见性 - volatile使用

##### 使用条件

- 对变量的写操作不依赖于当前值
- 该变量没有包含在具有其他变量的不变式中

> volatile适合作为状态标记量

```java
volatile boolean inited = false;
//线程1
context = loadContext();
inited = true;
//线程2
while(!inited){
    sleep();
}
doSomeThingWithConfig(context);
```

### 有序性

- Java内存模型中，允许编译器和处理器对指令进行重排序，但是重排序过程不会影响到单线程程序的执行，却会影响到多线程并发执行的正确性
- volatile，synchronized，Lock  

#### 有序性 - happens-before原则

- **程序次序规则**：一个线程内，按照代码顺序，书写在前面的操作先行发生于书写在后面的操作
- **锁定规则**：一个unLock操作先行发生于后面对同一个锁的lock操作
- **volatile变量规则**：对一个变量的写操作先行发生于后面对这个变量的读操作
- **传递规则**：如果操作A先行发生于操作B，而操作B又先行发生于操作C，则可以得出操作A先行发生于操作C
- 线程启动原则：Thread对象的start() 方法先行发生于此线程的每一个动作
- 线程中断操作：对线程的interrupt() 方法的调用先行发生于被中断线程的代码检测到中断事件的发生
- 线程终结规则：线程中所有的操作都先行发生于线程的终止检测，我们可以通过Thread.join() 方法结束、Thread.isAlive() 的返回值手段检测到线程已经终止执行
- 对象终结规则：一个对象的初始化完成先行发生于他的finalize() 方法的开始

### 线程安全性 - 总结

- 原子性：Atomic包、CAS算法、synchronized、Lock
- 可见性：synchronized、volatile
- 有序性：happens-before

## 安全发布对象

### 发布对象

- **发布对象**：使一个对象能够在当前范围之外的代码所使用
- **对象逸出**：一种错误的发布当一个对象还没有构造完成时，就使他被其它的线程所见

#### 发布对象 - 不安全示例

```java
package cn.mirror.concurrency.example.publish;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
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
```

```
[1, 2, 3]
[4, 2, 3]
```

#### 对象逸出示例

```java
package cn.mirror.concurrency.example.publish;

import lombok.extern.slf4j.Slf4j;

/**
 * @author mirror
 */
@Slf4j
public class Escape {
    private int thisCanBeEscape = 0;

    public Escape() {
        new InnerClass();
    }

    private class InnerClass {
        public InnerClass() {
            log.info("{}", Escape.this.thisCanBeEscape);
        }
    }
}
```



### 安全发布对象

- 在静态初始化函数中初始化一个对象引用
- 将对象的引用保存到volatile类型域或者AtomicReference对象中
- 将对象的引用保存到某个正确构造对象的final类型域中
- 将对象引用保存到一个由锁保护的域中 

 

```java
/**
 * 懒汉模式 volatile + 双重检查锁 线程安全
 */
@NotThreadSafe
public class SingletonExample4 {
    private volatile static SingletonExample4 instance = null;
    private SingletonExample4() {
    }
    public static SingletonExample4 getInstance() {
        if (instance == null) {
            synchronized (SingletonExample4.class) {
                if (instance == null) {
                    return instance = new SingletonExample4();
                }
            }
        }
        return instance;
    }
}
```

## 不可变对象

- 不可变对象满足的条件
  - 对象创建后其状态就不能修改
  - 对象所有域都是final类型
  - 对象时正确创建的（在对象创建期间，this引用没有逸出）
- final关键字：类，方法，变量
  - 修饰类：不能被继承
  - 修饰方法：1，锁定方法不被继承类修改；2，效率
  - 修饰变量：基本数据类型，引用类型变量

- 创建不可变对象
  - Collections.unmodifiableXXX: Collection, List, Map, Set...
  - Guava: ImmutableXXX: Collection, List, Map, Set...

```java
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
```

### 线程封闭

- Ad-hoc 线程封闭：程序控制实现，最糟糕，忽略
- 堆栈封闭：局部变量，无并发问题
- ThreadLocal 线程封闭：特别好的封闭方法

### 线程不安全类与写法

- StringBuilder -> StringBuffer
- SimpleDateFormat -> JodaTime
- ArrayList，HashSet，HashMap等Collections

- 先检查再执行：if(condition(a)){ handle(a); }

## 同步容器

- ArrayList -> Vector，Stack
- HashMap -> HashTable（key，Value不能为null)
- Collections.synchronizedXXX(List、Set、Map)

> 不能完全并发安全

## 线程安全 - 并发容器 J.U.C

- ArrayList -> CopyOnWriteArrayList

  > 读操作在原数组上进行，当有新的元素添加进来时，复制一个数组进行写操作，完成操作后将原有数组指向新的数组，add操作是在锁的保护下进行的
  >
  > 缺点：拷贝数组消耗内存，元素过多可能导致YoungGC或者FullGC，不能用于实时读的场景，适合读多写少的场景
  >
  > 思想：读写分离，最终一致性，使用时另外开辟空间解决并发冲突

  ```java
  public class CopyOnWriteArrayList<E>
      implements List<E>, RandomAccess, Cloneable...
    
  public boolean add(E e) {
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
          Object[] elements = getArray();
          int len = elements.length;
          Object[] newElements = Arrays.copyOf(elements, len + 1);
          newElements[len] = e;
          setArray(newElements);
          return true;
      } finally {
          lock.unlock();
      }
  }
  ```

  

- HashSet、TreeSet -> CopyOnWriteArraySet、ConcurrentSkipListSet

  > ` CopyOnWriteArraySet `底层使用 ` CopyOnWriteArrayList `，使用迭代器遍历时速度很快，并且不会和其它线程冲突

- Collections.synchronizedXXX(List、Set、Map)

