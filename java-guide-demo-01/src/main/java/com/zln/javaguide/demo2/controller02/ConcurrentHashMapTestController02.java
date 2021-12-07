package com.zln.javaguide.demo2.controller02;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author zln
 * @date 2021-11-26
 */
public class ConcurrentHashMapTestController02 {


    /**
     * 循环次数
     */
    private static final int LOOP_COUNT = 10000000;
    /**
     * 线程数量
     */
    private static final int THREAD_COUNT = 10;
    /**
     * 元素数量
     */
    private static final int ITEM_COUNT = 10;

    private static void norMaluse() throws InterruptedException {
        ConcurrentHashMap<String, Long> freqs = new ConcurrentHashMap<>(ITEM_COUNT);
        //并行执行的任务框架 将大任务分成若干小任务
        ForkJoinPool forkJoinPool = new ForkJoinPool(THREAD_COUNT);
        // rangeClosed()  [1,LOOP_COUNT] 闭区间
        // 使用并行流计算
        forkJoinPool.execute(() -> IntStream.rangeClosed(1, LOOP_COUNT).parallel().forEach(i -> {
                    //获得一个随机的Key
                    String key = "item" + ThreadLocalRandom.current().nextInt(ITEM_COUNT);
                    synchronized (freqs) {
                        if (freqs.containsKey(key)) {
                            //Key存在则+1
                            freqs.put(key, freqs.get(key) + 1);
                        } else {
                            //Key不存在则初始化为1
                            freqs.put(key, 1L);
                        }
                    }
                }
        ));
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.HOURS);
    }

    /**
     * 关于 computeIfAbsent 的说明
     * // java8之前。从map中根据key获取value操作可能会有下面的操作
     * Object key = map.get("key");
     * if (key == null) {
     *     key = new Object();
     *     map.put("key", key);
     * }
     *
     * // java8之后。上面的操作可以简化为一行，若key对应的value为空，会将第二个参数的返回值存入并返回
     * Object key2 = map.computeIfAbsent("key", k -> new Object());
     *
     * */
    private static Map<String, Long> goodUse() throws InterruptedException {
        // LongAdder 1.8新增的一个原子性操作类
        // 用于解决 高并发的请求下AtomicLong的性能问题
        ConcurrentHashMap<String, LongAdder> freqs = new ConcurrentHashMap<>(ITEM_COUNT);
        ForkJoinPool forkJoinPool = new ForkJoinPool(THREAD_COUNT);
        forkJoinPool.execute(() -> IntStream.rangeClosed(1, LOOP_COUNT).parallel().forEach(i -> {
                    String key = "item" + ThreadLocalRandom.current().nextInt(ITEM_COUNT);
                    //用computeIfAbsent()方法来实例化LongAdder，然后利用LongAdder来进行线程安全计数
                    freqs.computeIfAbsent(key, k -> new LongAdder()).increment();
                }
        ));
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.HOURS);
        //因为我们的Value是LongAdder而不是Long，所以需要做一次转换才能返回
        return freqs.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().longValue())
                );
    }


    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        //norMaluse();
        goodUse();
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
    }
}
