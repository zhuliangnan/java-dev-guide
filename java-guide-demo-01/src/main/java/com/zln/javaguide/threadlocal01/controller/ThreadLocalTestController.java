package com.zln.javaguide.threadlocal01.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zln
 * @date 2021-11-26
 */
@RestController
public class ThreadLocalTestController {


    /**
     * Lambda构造方式
     *  //相当于以下代码
     *  ThreadLocal<Integer> currentUser = new ThreadLocal<>();
     *  currentUser.set(null);
     * */
    private static final ThreadLocal<Integer> currentUser = ThreadLocal.withInitial(() -> null);


    @GetMapping("wrong1")
    protected Map wrong(@RequestParam("userId") Integer userId) {
        //设置用户信息之前先查询一次ThreadLocal中的用户信息
        String before  = Thread.currentThread().getName() + ":" + currentUser.get();
        //设置用户信息到ThreadLocal
        currentUser.set(userId);
        //设置用户信息之后再查询一次ThreadLocal中的用户信息
        String after  = Thread.currentThread().getName() + ":" + currentUser.get();
        //汇总输出两次查询结果
        Map result = new HashMap(16);
        result.put("before", before);
        result.put("after", after);
        return result;
    }

    @GetMapping("right1")
    protected Map nowrong(@RequestParam("userId") Integer userId) {
        //设置用户信息之前先查询一次ThreadLocal中的用户信息
        String before  = Thread.currentThread().getName() + ":" + currentUser.get();
        //设置用户信息到ThreadLocal
        currentUser.set(userId);
        try {
            //设置用户信息之后再查询一次ThreadLocal中的用户信息
            String after  = Thread.currentThread().getName() + ":" + currentUser.get();
            //汇总输出两次查询结果
            Map result = new HashMap(16);
            result.put("before", before);
            result.put("after", after);
            return result;
        }finally {
            currentUser.remove();
        }
    }

}
