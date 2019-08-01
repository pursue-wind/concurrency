## 线程安全性

当多个线程访问某个类时，不管运行时环境采用何种调度方式或者这些进程将如何交替执行，并且在主调代码中不需要任何额外的同步或协调，这个类都能表现出正确的行为，那么就称这个类是线程安全的。

线程安全性主要体现：


> 原子性：提供了互斥访问，同一时刻只能有一个线程来对它进行操作
可见性：一个线程对主内存的修改可以及时的被其他线程观察到
有序性：一个线程观察其他线程中的指令执行顺序，由于指令重排序的存在，该观察结果一般是杂乱无序

### 原子性 - Atomic 包

>  Atomic包 位于java.util.concurrent.atomic AtomicXXX :

>  CAS、Unsafe.compareAndSwapXXX   

>  CAS（Compare and swap）比较和替换是设计并发算法时用到的一种技术。简单来说，比较和替换是使用一个期望值和一个变量的当前值进行比较，如果当前变量的值与我们期望的值相等，就使用一个新值替换当前变量的值。

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
/**
 * @param var1 对象
 * @param var2 偏移量
 * @param var3 期望值
 * @param var5 修改值
 */
public final native boolean compareAndSwapInt(Object var1, long var2, int var4, int var5);
```

**AtomicInLong 与 LongAdder 比较**

就像我们所知道的那样,`AtomicLong`的原理是依靠底层的`CAS`来保障原子性的更新数据，在要添加或者减少的时候，会使用死循环不断地`CAS`到特定的值，从而达到更新数据的目的。如果竞争不激烈，修改成功几率很高，否则失败概率很高，在失败几率很高的情况下，这些原子操作就会进行多次的循环操作尝试，因此性能会受到影响。

对于普通类型的`Long`和`Doubble`变量，JVM允许将64位的读操作或写操作拆成两个三十二位的操作。

`LongAdder`的核心是将热点数据分离，比如说它可以将`AtomicLong`内部核心数据value分离成一个数组，每个线程访问时，通过hash等算法，映射到其中一个数字进行计数，最终的计数结果则会这个数据的求和累加，其中热点数据value会被分离成多个cell，每个cell独自维护内部的值，当前对象实际值为所有cell累计合成，这样的话，热点就进行了有效的分离，并提高了并行度。

`LongAdder`在`AtomicLong`的基础上将单点的更新压力分散到各个节点，在低并发的时候通过对base的直接更新可以很好的保障和`AtomicLong`的性能基本保持一致，而在高并发的时候通过分散提高了性能。

缺点是`LongAdder`在统计的时候如果有并发更新，可能导致统计的数据有误差。

实际使用中，在处理高并发时，可以优先使用`LongAdder`，而不是继续使用`AtomicLong`，当然，在线程竞争很低的情况下，使用`AtomicLong`更简单更实际一些，并且效率会高些。其他情况下，比如序列号生成，这种情况下需要准确的数值，全局唯一的`AtomicLong`才是正确的选择，而不是`LongAdder`

#### AtomicReference：原子性引用

```
@Slf4j
@ThreadSafe
public class AtomicReferenceExample {
    private static AtomicReference<Integer> count = new AtomicReference<>(0);
    public static void main(String[] args) {
        count.compareAndSet(0, 2); // 2
        count.compareAndSet(0, 1); // no
        count.compareAndSet(1, 3); // no
        count.compareAndSet(2, 4); // 4
        count.compareAndSet(3, 5); // no
        log.info("count:{}", count.get());
    }
}
```

以上实例比较简单，我有个疑问？假如我们引用的是一个自定义的对象，并且对象里面有属性值，然后，修改对象中的属性值也是原子性的吗？还是只是对对象的引用是原子性操作。

通过源码分析，可以得出

- `AtomicReference `所提供的某些方法可以进行原子性操作，如`compareAndSet`、`getAndSet`，这仅仅是对引用进行原子性操作
- `AtomicReference` 不能保证对象中若存在属性值修改是线程安全的，如假设引用对象是`person`，修改`person`中`name`和`age`，多个线程同时从引用中获得对象，并进行修改，会出现线程不安全情况。

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

#### 锁

  JAVA中能保证同一时刻，只有一个线程来进行对其进行操作的，除了`atomic`包中所提供的类之外，还有`jdk`提供的锁，JAVA主要提供以下锁：

1. `synchronized` : 关键字，并且依赖与`JVM`，**作用对象的作用范围内**都是同一时刻只能有一个线程对其操作的
2. `Lock` : 接口类，依赖特殊的CPU指令，使用代码实现，常用子类`ReentrantLock`

**synchronized**

- 修饰代码块：大括号括起来的代码，也称同步代码块，作用与**调用的对象**
- 修饰方法：整个方法，也称同步方法，作用与**调用的对象**
- 修饰静态方法：整个静态方法，作用于**类的所有对象**
- 修饰类：括号括起来的部分，作用与**类的所有对象**



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

- volatile进行加操作线程不安全的，不适合计数场景
- volatile关键字不具有原子性

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

> 多线程并发环境下，线程安全极为重要。往往一些问题的发生都是由于不正确的发布了对象造成了对象逸出而引起的，因此如果系统开发中需要发布一些对象，必须要做到安全发布，以免造成安全隐患。

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

在上面线程不安全类中，提到了`ArrayList`、`HashSet`、`HashMap`非线程安全的容器，如果有多个线程并发的访问，就会出现线程安全问题，因此在编写程序的时候，必须要求开发人员手动的在任何访问这些容器的地方进行同步处理，导致使用这些容器非常不便，因此JAVA中提供同步容器。

- ArrayList -> Vector，Stack
- HashMap -> HashTable（key，Value不能为null)
- Collections.synchronizedXXX(List、Set、Map)

> 同步容器中的方法主要采取`synchronized`进行同步，因此执行的性能会收到受到影响，并且同步容器并**不一定能做到真正的线程安全**。

## 线程安全 - 并发容器 J.U.C

所谓的J.U.C其实是JDK所提供的一个包名，全程为`java.util.concurrent`,里面提供了许多线程安全的集合。

- ArrayList -> CopyOnWriteArrayList

  > 读操作在原数组上进行，当有新的元素添加进来时，复制一个数组进行写操作，完成操作后将原有数组指向新的数组，add操作是在锁的保护下进行的
  >
  > 缺点：拷贝数组消耗内存，元素过多可能导致`YoungGC`或者`FullGC`，不能用于实时读的场景，适合**读多写少**的场景
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

  > ` CopyOnWriteArraySet `底层使用 ` CopyOnWriteArrayList `，适合比较小的集合，其中所有可变操作（add、set、remove等等）都是通过对底层数组进行一次新的复制来实现的,一般需要很大的开销。迭代器支持hasNext(), next()等不可变操作，不支持可变的`remove`操作。使用迭代器遍历时速度很快，并且不会和其它线程冲突
  
  > ConcurrentSkipListSet是1.6新增的类，和TreeSet一样，支持自然排序，可以从构造里面传入比较器，居于Map集合，对于批量操作如addAll()，removeAll()，containsAll()，并不能保证原子性

  ```
  public class CopyOnWriteArraySet<E> extends AbstractSet<E>
  
  private final CopyOnWriteArrayList<E> al;
  
  public CopyOnWriteArraySet() {
  	al = new CopyOnWriteArrayList<E>();
  }
  ```

- HashMap、TreeMap -> ConcurrentHashMap、ConcurrentSkipListMap

  > **ConcurrentHashMap**不允许`null`值，绝大部分使用`Map`都是读取操作，而且读操作大多数都是成功的，因此，`ConcurrentHashMap`针对读操作进行了大量的优化。在高并发的场景下，有很大的优势。
  
  > **ConcurrentSkipListMap**内部使用``SkipList`结构实现的。跳表是一个链表，但是通过使用“跳跃式”查找的方式使得插入、读取数据时复杂度变成了O(log N)。
  >
  > **跳表（SkipList）：使用“空间换时间”的算法，令链表的每个结点不仅记录next结点位置，还可以按照level层级分别记录后继第level个结点。**
  >
  > `ConcurrentSkipListMap`有几个`ConcurrentHashMap`不能比拟的**优点**：
  >
  > 1. `ConcurrentSkipListMap` 的key是有序的，而`ConcurrentHashMap`不是
  > 2. `ConcurrentSkipListMap` 支持更高的并发。`ConcurrentSkipListMap`的存取时间是`log（N）`，和线程数几乎无关。也就是说在数据量一定的情况下，并发的线程越多，`ConcurrentSkipListMap`越能体现出他的优势。

### 安全共享对象策略 - 总结

- 线程限制：一个被线程限制的对象，由线程独占，并且只能被占有它的线程修改
- 共享只读：一个共享只读的对象，在没有额外同步的情况下，可以被多个线程并发访问，但是任何线程都不能修改它
- 线程安全对象：一个线程安全的对象或者容器，在内部通过同步机制来保证线程安全，所以其它线程无需额外的同步就可以通过公共接口随意访问它
- 被守护对象：被守护对象只能通过获取特定的锁来访问

## JUC

### AbstractQueuedSynchronizer - AQS

> 底层使用了双向链表实现一个SyncQueue同步队列，包括head节点（waitStatus，prev，next，thread，nextWaiter）和tail节点，head节点主要用于后续的调度

> 还有一个ConditionQueue（单向链表），只有当程序中需要使用到Condition的时候才会创建一个或者多个ConditionQueue

> 内部维护一个CLH队列来管理锁，线程首先尝试获取锁，如果失败，就将线程以及等待状态等信息包装成一个Node节点，加入到SyncQueue同步队列，接着会不断的循环尝试获取锁，条件是当前节点为head的直接后继才会尝试，如果失败就会阻塞直到被唤醒，持有锁的线程释放锁的时候会唤醒后继的线程

- 使用Node实现FIFO队列，可以用来构建锁或者其它相关的同步装置的基础框架

- 利用了一个int类型表示状态（state，获取锁的线程数，0表示没有线程获取锁，1表示有线程获取锁，大于1表示重入锁的数量）

- 使用方法是继承（基于模板方法设计，使用者需要继承并复写其中的方法）

- 子类通过继承并通过实现它的方法管理其状态（acquire和release）方法操纵状态

- 可以同时实现排它锁和共享锁模式（独占、共享）

  > ReentrantReadWriteLock通过两个内部类分别实现读锁和写锁

### AQS同步组件

- CountDownLatch：通过计数保证线程是否需要一直阻塞
- Semaphore：控制同一时间线程并发的数目
- CyclicBarrier：类似CountDownLatch，能阻塞线程
- **ReentrantLock**
- Condition：使用时需要`ReentrantLock`
-  FutureTask

### AQS - CountDownLatch

> 调用await() 的线程会一直阻塞，直到调用countDown() 把计数器减到0

> 计数器无法重置

应用：并行计算，当某个任务需要处理运算量非常大，可以将该运算任务拆分为多个子任务，等待所有的子任务完成之后，父任务再拿到所有子任务的运算结果进行汇总。利用`CountDownLatch`可以保证任务都被处理完才去执行最终的结果运算，过程中每一个线程都可以看做是一个子任务。

### AQS - Semaphore

> 可以控制并发访问的个数， aquire() 获得一个许可，release() 释放一个许可，实现有限大小的链表

```java
//尝试获得许可    
public boolean tryAcquire(){...}
public boolean tryAcquire(long timeout, TimeUnit unit){...}
public boolean tryAcquire(int permits) {...}
public boolean tryAcquire(int permits, long timeout, TimeUnit unit){...}
```

应用：常用于仅能提供有限访问的资源，如数据库。

### AQS - CyclicBarrier

> 允许一组线程相互等待，直到到达某个公共屏障点（comonBarrierPoiter）

一种同步辅助工具，允许一组线程等待彼此达到`common barrier point `。`CyclicBarriers`在涉及固定大小的线程方的程序中非常有用必须偶尔等待彼此。屏障称为cyclic，因为它可以在释放等待线程后重复使用。 

当某个线程调用`await()`方法之后，该线程就进入等待状态，而且计数器是执行加一操作，当计数器值达到初始值（设定的值），因为调用`await()`方法进入等待的线程，会被唤醒，继续执行他们后续的操作。由于`CyclicBarrier`在等待线程释放之后，可以进行重用，所以称之为循环屏障。它非常适用于一组线程之间必需经常互相等待的情况。

### Semaphore与CyclicBarrier比较

**相同点：**

1. 都是同步辅助类。
2. 使用计数器实现

**不同点：**

1. `CountDownLatch`允许一个或多个线程，等待其他一组线程完成操作，再继续执行。
2. `CyclicBarrier`允许一组线程相互之间等待，达到一个共同点，再继续执行。
3. `CountDownLatch`不能被复用
4. `CyclicBarrier`适用于更复杂的业务场景，如计算发生错误，通过重置计数器，并让线程重新执行
5. `CyclicBarrier`还提供其他有用的方法，比如`getNumberWaiting`方法可以获得`CyclicBarrier`阻塞的线程数量。`isBroken`方法用来知道阻塞的线程是否被中断。

**场景比较：**

1. `CyclicBarrier` : 好比一扇门，默认情况下关闭状态，堵住了线程执行的道路，直到所有线程都就位，门才打开，让所有线程一起通过。
2. `CyclicBarrier`可以用于多线程计算数据，最后合并计算结果的应用场景。比如我们用一个Excel保存了用户所有银行流水，每个Sheet保存一个帐户近一年的每笔银行流水，现在需要统计用户的日均银行流水，先用多线程处理每个sheet里的银行流水，都执行完之后，得到每个sheet的日均银行流水，最后，再用`barrierAction`用这些线程的计算结果，计算出整个Excel的日均银行流水。
3. `CountDownLatch` : 监考老师发下去试卷，然后坐在讲台旁边玩着手机等待着学生答题，有的学生提前交了试卷，并约起打球了，等到最后一个学生交卷了，老师开始整理试卷，贴封条

## ReentrantLock 与 锁

- ReentrantLock（可重入锁）和 synchronized 区别

  - 可重入性
  - 锁的实现
  - 性能的区别
  - 功能区别

  > 避免进入内核态的阻塞状态

- ReentrantLock 独有的功能

  - 可指定是公平锁还是非公平锁
  - 提供一个Condition 类，可以分组唤醒需要唤醒的线程
  - 提供能够中断等待锁的线程的机制，lock.lockInterruptibly()
  
### ReentrantLock

  > **boolean tryLock()** 
  >
  > *只有在调用时没有被另一个线程持有时才获取锁。*
  >
  > **boolean tryLock(long timeout, TimeUnit unit)**
  >
  > *如果在给定的等待时间内没被另一个线程持有并且当前线程未被中断，则获取锁。*
  >
  > **lockInterruptibly()**
  >
  > *除非当前线程是中断的，否则获取锁。*
  >
  > **isLocked()**
  >
  > *查询此锁是否由任何线程持有。*
  >
  > **isFair()**
  >
  > *如果此锁定的公平性设置为true，则返回true。*
  >
  > **hasQueuedThreads()**
  >
  > *查询是否有任何线程正在等待获取此锁。请注意因为取消可能在任何时候发生。*

  ```java
  public ReentrantLock() { 
    sync = new NonfairSync(); 
  }
  public ReentrantLock(boolean fair) {
  	sync = fair ? new FairSync() : new NonfairSync();
  }
  ```

  ```java
  public class ReentrantLock implements Lock, java.io.Serializable {
      private final Sync sync;
      abstract static class Sync extends AbstractQueuedSynchronizer {
          abstract void lock();
          final boolean nonfairTryAcquire(int acquires) {
              final Thread current = Thread.currentThread();
              int c = getState();
              if (c == 0) {
                  if (compareAndSetState(0, acquires)) {
                      setExclusiveOwnerThread(current);
                      return true;
                  }
              }
              else if (current == getExclusiveOwnerThread()) {
                  int nextc = c + acquires;
                  if (nextc < 0) // overflow
                      throw new Error("Maximum lock count exceeded");
                  setState(nextc);
                  return true;
              }
              return false;
          }
       }
    	static final class NonfairSync extends Sync {
          final void lock() {
              if (compareAndSetState(0, 1))
                  setExclusiveOwnerThread(Thread.currentThread());
              else
                  acquire(1);
          }
  
          protected final boolean tryAcquire(int acquires) {
              return nonfairTryAcquire(acquires);
          }
      }
  }
  ```

#### ReentrantLock 的使用

```java
private static void test() {
    try {
        LOCK.lock();
        count++;
    } finally {
        LOCK.unlock();
    }
}
```

#### Condition

```
new Thread(() -> {
    try {
        reentrantLock.lock();
        log.info("wait signal"); // 1
        condition.await(); //将线程加入到 Condition 等待队列中
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    log.info("get signal"); // 4
    reentrantLock.unlock();
}).start();

new Thread(() -> {
    reentrantLock.lock();
    log.info("get lock"); // 2
    try {
        Thread.sleep(3000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    condition.signalAll(); //唤醒等待队列的线程，加入到AQS的等待队列中
    log.info("send signal ~ "); // 3
    reentrantLock.unlock();
}).start();
```



### ReentrantReadWriteLock

> 读取锁定：没有任何读写锁的时候才可以获得写入锁，可能造成写饥饿 

```java
public class ReentrantReadWriteLock implements ReadWriteLock{
    private final ReentrantReadWriteLock.ReadLock readerLock;
    private final ReentrantReadWriteLock.WriteLock writerLock;
    final Sync sync;
}
```

### StampedLock - since 1.8

基于功能的锁，具有三种控制读/写/访问的模式。 StampedLock的状态包括版本和模式。 锁定获取方法返回一个代表和控制锁定状态访问的标记;这些方法的“尝试”版本可能会返回特殊值零以表示无法获取访问权限。锁定释放和转换方法需要标记作为参数，如果它们与锁定状态不匹配则会失败。这三种模式是：

- 写锁。方法#writeLock 可能阻止等待独占访问，返回可在方法#unlockWrite中使用的戳记以释放锁定。还提供了tryWriteLock的不定时和定时版本。当锁定处于写入模式时，不会获得读锁定，并且所有乐观读取验证都将失败。

- 读锁。方法#readLock可能会阻止等待非独占访问，返回可以在方法#unlockRead中使用的戳记以释放锁定。还提供了tryReadLock的不定时和定时版本。

- 乐观读。方法#tryOptimisticRead仅当在写入模式下当前未保持锁定时才返回非零标记。如果自获取给定标记以来未在写入模式下获取锁，则方法#validate返回true。这种模式可以被认为是读锁的极弱版本，可以随时由作者打破。对短的只读代码段使用乐观模式通常可以减少争用并提高吞吐量。但是，它的使用本质上是脆弱的。乐观读取部分应该只读取字段并将它们保存在局部变量中，以便以后在验证后使用。在乐观模式下读取的字段可能非常不一致，因此只有在您熟悉数据表示以检查一致性或重复调用方法validate()时才会使用。例如，在首次读取对象或数组引用，然后访问其中一个字段，元素或方法时，通常需要执行此类步骤。

#### StampedLock 使用

```java
private static void test() {
    long stamp = LOCK.writeLock();
    try {
        count++;
    } finally {
        LOCK.unlock(stamp);
    }
}
```

### FutureTask

- Callable 与 Runnable 接口对比
- Future接口
- FutureTask类

```java
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
```

### Fork/Join 框架

#### 工作窃取算法

> 假如需要做一个比较大的任务，可以把任务分割为若干互不依赖的子任务，为了减少线程间的竞争，把这些子任务分别放到不同的队列里，并为每个队列创建一个单独的线程来执行队列里的任务，线程和队列一一对应。
>
> 比如A线程负责处理A队列里的任务。但是，有的线程会先把自己队列里的任务干完，而其他线程对应的队列里还有任务等待处理。干完活的线程与其等着，不如去帮其他线程，于是他就去其他线程的队列里窃取一个任务来执行。
>
> 而在这时他们会访问同一个队列，所以为了减少窃取任务线程和被窃取任务线程之间的竞争，通常会使用双端队列，被窃取任务线程永远从双端队列的头部拿任务执行，而窃取任务的线程永远从双端队列的尾部拿任务执行。
>
> - 工作窃取算法的优点：充分利用线程进行并行计算，减少了线程间的竞争。
> - 工作窃取算法的缺点：在某些情况下还是存在竞争，比如双端队列里只有一个任务时。并且该算法会消耗了更多的系统资源，比如创建多个线程和多个双端队列。

```java 
public class ForkJoinTaskExample extends RecursiveTask<Integer> {

    public static final int threshold = 2;
    private int start;
    private int end;

    public ForkJoinTaskExample(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        int sum = 0;

        //如果任务足够小就计算任务
        boolean canCompute = (end - start) <= threshold;
        if (canCompute) {
            for (int i = start; i <= end; i++) {
                sum += i;
            }
        } else {
            // 如果任务大于阈值，就分裂成两个子任务计算
            int middle = (start + end) / 2;
            ForkJoinTaskExample leftTask = new ForkJoinTaskExample(start, middle);
            ForkJoinTaskExample rightTask = new ForkJoinTaskExample(middle + 1, end);

            // 执行子任务
            leftTask.fork();
            rightTask.fork();

            // 等待任务执行结束合并其结果
            int leftResult = leftTask.join();
            int rightResult = rightTask.join();

            // 合并子任务
            sum = leftResult + rightResult;
        }
        return sum;
    }

    public static void main(String[] args) {
        ForkJoinPool forkjoinPool = new ForkJoinPool();

        //生成一个计算任务，计算1+2+3+4
        ForkJoinTaskExample task = new ForkJoinTaskExample(1, 1000);

        //执行一个任务
        Future<Integer> result = forkjoinPool.submit(task);

        try {
            log.info("result:{}", result.get());
        } catch (Exception e) {
            log.error("exception", e);
        }
    }
}
```

### BlockQueue

- ArrayBlockingQueue
- DelayQueue
- LinkedBlockingQueue
- PriorityBlockingQueue
- SynchronousQueue



## 线程池

- new Thread弊端
  - 每次new Thread 新建对象，性能差
  - 线程缺乏统一管理，可能无限制的新建线程，相互竞争，又可能占有过多系统资源导致死机或OOM
  - 缺少更多功能，如更多功能、定期执行、线程中断
- 线程池的好处
  - 重用存在的线程，减少对象的创建、消亡的开销，性能佳
  - 可有效控制最大并发线程数，提高系统资源利用率，同时可以避免过多资源竞争，避免阻塞
  - 提供定时执行、定期执行、单线程、并发控制等功能

### 线程池 - ThreadPoolExecutor

- corePoolSize：核心线程数量
- maximumPoolSize：线程最大线程数
- workQueue：阻塞队列，存储等待执行的任务，会对线程池运行过程产生重大影响
- keepAliveTime：线程没有任务执行时最多保持多久时间终止
- threadFactory：线程工厂，用来创建线程
- rejectHandler：当拒绝处理任务时的策略

```java
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler) {}
```

- execute()：提交任务，交给线程池执行
- submit()：提交任务，能够返回执行结果 execute+Future
- shutdown()：关闭线程池，等待任务都执行完
- shutdownNow()：关闭线程池，不等待任务执行完



- getTaskCount()：线程池已执行和未执行的任务总数
- getCompletedTaskCount：已完成的任务数量
- getPoolSize：线程池当前的线程数量
- getActiveCount()：当前线程池中正在执行任务的线程数量

### 线程池 - Executor框架接口

- Executors.newCachedThreadPool
- Executors.newFixedThreadPool
- Executors.newScheduledThreadPool
- Executors.newSingleThreadPool

