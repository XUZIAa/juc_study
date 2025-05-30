# JUC工具类

高并发 多线程  充分利用cpu 减少cpu的空闲时间

进程：操作系统的基本分配单元，每个进程有唯一的进程标识符PID,区分不同的进程，一个进程包含多个进程

线程：是cpu调度和分派的基本单位，一个cpu内核只能处理一个线程，所有线程共享进程的资源

协程：在线程内部可以创建多个协程，共享一个线程的资源，java19支持协程



并发：多个线程同时发送请求，==一个核心==轮流完成请求 ，即宏观上多个线程同时运行，微观上分时交替执行

并行：多个线程同时发送请求，==多个核心==接受请求，同时执行

串行：多个线程同时发送请求，一个核心按照下单顺序依次完成请求，线程排队等待



高并发 低耗时 建议少线程  低并发 高耗时 多线程



  实现runnable方法 但是得再把它传给thread 再调用start方法    （runnable只有一个抽象方法）

  实现thread类，调用start方法





### ==thread的各种方法==

==run是同步方法 start是异步方法== run方法存放任务代码 start方法启动线程 start只能执行一个 

thread中有setName getName 可以设置 获取当前线程的名字 在哪运行获取的就是哪个线程的名字

thread中的sleep 作用让出时间片 防止一直占用  在那运行就睡眠哪个线程  t1.sleep(1000)  这个1000代表睡眠时间 参数毫秒 一般用的少 通过mq消息队列有相同的功能 使用sleep必须捕获interruptedexception异常 

thread 的interrupt 线程中断方法

thread.Interrupted 判断是否被中断，==清除==打断标记

thread.isInterrupted 判断是否被中断，==不清除==打断标记

thread.yield 让出暂时或放弃当前拥有的cpu资源，执行其他线程，当前愿意让出cpu资源，是否让出还是看cpu。

thread.setProiority  getPriority   设置 获取线程的优先级  优先级为1-10 越高越能提高该线程被cpu调度的几率

thread.join  等待该线程结束再执行其他线程的代码，==**不要**在一个线程的 `run()` 方法中 `join` 该线程自身。，否则导致死锁==

thread.isAlive 判断线程是否存活

thread.setDaemon 设置为守护线程

thread.isDaemon 查看是否为守护线程 比如垃圾回收器线程就是守护线程，==守护线程的生命周期依赖于所有非守护线程==

thread.getState 获取线程的状态 ==分别是 new 新建  没有执行start方法	 runnable就绪  可能在等待 可能正在运行	blocoed阻塞  没有获取到锁资源	waiting等待  	timewaiting超时等待 	terminated 终止== 6+.60



## Callable接口 thread和runnable 无法返回结果 callable可以返回结果

提供了一个call方法   

==callable不能直接直接传给thread的构造函数 可以使用Future类来包装callable类  再传送给thread  之后可以通过Future的get()方法获取返回值==

 如果线程没有执行完 get()方法会等待

   

##  线程池 池化技术 复用了线程资源 优化了性能

新建线程池  

```JAVA
ExecutorService pool1=new ThreadPoolExecutor(10, //corePoolSize
                                             20, //maximumPoolSize
                                             0L, //keepAliveTime
                                             TimeUnit.SECONDS,
                                             new ArrayBlockingQueue<Runnable>(3),
                                             Executors.defaultThreadFactory(),
                                             new ThreadPoolExecutor.DiscardPolicy()
                                            );
    
```



执行线程 调用线程池的execute方法 传送==一个实现了runnable接口的线程==



==执行后一定要关闭线程池！！！ poll.shutdown(); 要写在finally里 因为finally一定执行 showdown()会立刻关闭== 



线程池的execute submit 方法

execute的参数只允许runnable submit只允许callable

execute会在子线程中抛出异常 主线程捕获不到 submit不会在子线程中抛出异常 而是暂时存起来 在future.get的时候抛出异常



## 线程安全

多线程并发同时对共享数据进行读写，会造成数据混乱，此时线程不安全



 ==原子性==

操作是单一不可分割的操作。否则线程不安全。 



==有序性==

按照代码的顺序依次进行执行 防止指令重排（对二进制指令码进行重排，会导致顺序改变）



==可见性==

当多个线程访问同一个变量时，一个线程修改了这个变量的值，其他线程能够立刻看得到修改的值



## ThreadLocal

就是一个线程的局部变量

使用后要使用remove方法清除 否则会导致内存溢出

ThreadLocal创建在最外面   里面每个线程调用它 都是存在一个线程内的单独空间



## Volatile关键字

实现有序性和防止指令重排



## 原子类

缩小在了变量级别

juc.atomic下

提供了很多数据类型

提供了很多方法，不像普通得数据类型



## 乐观锁

CAS就是乐观锁 比较交换

原子类自带CompareAndSet方法 比较交换 实现了cas的原子类



## 自旋锁 do while 循环 极端情况会产生ABA问题

当一个线程在获取锁的时候，如果锁已经被其他线程获取，那么该线程将一直循环等待，不断判断锁是否已经能够被获取，直到获取到锁才会退出循环。

和互斥锁比较类似 无论是互斥锁还是自旋锁在任意时刻，最多只能有一个线程获得锁。

 ABA问题解决可以加标记





## Wait和sleep的区别

sleep是线程的方法 wait是object的方法

sleep必须设置参数时间，wait可以不设置一直休眠

sleep不会释放锁资源 wait会释放锁资源

sleep不需要唤醒，wait需要



## synchronized锁升级

随着并发量的提升，synchronized会自动升级，根据锁的竞争情况，从无锁状态逐步升级到偏向锁、轻量级锁，最后才到重量级锁。，提升安全性，但是效率会变低。





## ReentrantLock

使用ReentrantLock需要 创建一个ReentrantLock对象 再使用它的lock 和unlock方法 保证了线程安全 更加灵活

   

可以通过tryLock() 显示锁申请限时 等待固定的时间



ReentrantLock获取锁的过程是可以中断的，在发起获取锁请求还未获取到锁的这段时间内可以中断请求。 使用interrupt()方法 中断标志为true



## 公平锁和非公平锁

大多数情况下是非公平锁，等待队列随机挑选一个运行，synchronized就是非公平锁。reentrantLock是公平锁

 非公平锁效率更高，充分利用cpu的时间片



## 排他锁和独占锁

排他锁就是独占锁，获取了锁既能读又能写，其他的线程不能读写，synchronized就是排他锁

共享锁都能读取锁资源

ReentrantReadWriteLock.ReadLock这个对象就是读锁  .WriteLock就是写锁

读锁是共享锁  写锁是排他锁

一个线程获取**写锁**后，其他线程的**读写**都会被阻塞。

一个线程获取**读锁**后，其他线程可以获取**读锁**，但不能获取**写锁**



## Synchronized和lock的区别

synchronized是关键字 lock是类

synchronized无法判断是否持有锁 lock能判断

synchroinzed的锁可重入 不可中断 非公平 lock的可重入 可中断 可公平可不公平
