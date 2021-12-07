package com.zln.javaguide.demo2.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author zln
 * @date 2021-11-30
 */
@RestController
@Slf4j
public class ControllerDemo2Test {

    @Data
    @RequiredArgsConstructor
    static class Item {
        final String name; //商品名
        int remaining = 1000; //库存剩余
        @ToString.Exclude //ToString不包含这个字段
        ReentrantLock lock = new ReentrantLock();
    }

    private ConcurrentHashMap<String,Item> items = new ConcurrentHashMap<>();

    /**
     * 初始化10个商品的商品库
     * */
    public ControllerDemo2Test() {
        IntStream.range(0, 10).forEach(i -> items.put("item" + i, new Item("item" + i)));
    }


    /**
     * 模拟在购物车进行商品选购，每次从商品清单（items 字段）中随机选购三个商品
     * */
    private List<Item> createCart() {
        return IntStream.rangeClosed(1, 3)
                // mapToObj返回值：该函数返回一个对象值的Stream，其中包含应用给定函数的结果。
                .mapToObj(i -> "item" + ThreadLocalRandom.current().nextInt(items.size()))
                .map(name -> items.get(name)).collect(Collectors.toList());
    }



    /**
     *
     * 先声明一个 List 来保存所有获得的锁，然后遍历购物车中的商品依次尝试获得商品的锁，
     * 最长等待 10 秒，获得--全部锁--之后再扣减库存；如果有无法获得锁的情况则解锁之前获得的所有锁，返回 false 下单失败。
     *
     * */
    private boolean createOrder(List<Item> order) {
        //存放所有获得的锁
        List<ReentrantLock> locks = new ArrayList<>();

        for (Item item : order) {
            try {
                //获得锁10秒超时
                if (item.lock.tryLock(10, TimeUnit.SECONDS)) {
                    locks.add(item.lock);
                } else {
                    locks.forEach(ReentrantLock::unlock);
                    return false;
                }
            } catch (InterruptedException e) {
            }
        }
        //锁全部拿到之后执行扣减库存业务逻辑
        try {
            order.forEach(item -> item.remaining--);
        } finally {
            locks.forEach(ReentrantLock::unlock);
        }
        return true;
    }



    @GetMapping("/02/02/wrong")
    public long wrong() {
        long begin = System.currentTimeMillis();
        //并发进行100次下单操作，统计成功次数
        long success = IntStream.rangeClosed(1, 100).parallel()
                .mapToObj(i -> {
                    // 生成购物车
                    List<Item> cart = createCart();
                    // 下单
                    return createOrder(cart);
                })
                .filter(result -> result)
                .count();
        log.info("success:{} totalRemaining:{} took:{}ms items:{}",
                success,
                items.entrySet().stream().map(item -> item.getValue().remaining).reduce(0, Integer::sum),
                System.currentTimeMillis() - begin, items);
        return success;
    }




    @GetMapping("/02/02/right")
    public long right() {
        long begin = System.currentTimeMillis();
        //并发进行100次下单操作，统计成功次数
        long success = IntStream.rangeClosed(1, 100).parallel()
                .mapToObj(i -> {
                    // 生成购物车
                    List<Item> cart = createCart().stream()
                            //排序
                            .sorted(Comparator.comparing(Item::getName))
                            .collect(Collectors.toList());
                    // 下单
                    return createOrder(cart);
                })
                .filter(result -> result)
                .count();
        log.info("success:{} totalRemaining:{} took:{}ms items:{}",
                success,
                items.entrySet().stream().map(item -> item.getValue().remaining).reduce(0, Integer::sum),
                System.currentTimeMillis() - begin, items);
        return success;
    }

}
