package com.zln.javaguide.demo1.controller;

import com.zln.javaguide.demo1.DataTest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.IntStream;

/**
 * @author zln
 * @date 2021-11-30
 */
@RestController
public class ControllerTest {

    @GetMapping("/02/wrong")
    public int wrong(@RequestParam(value = "count", defaultValue = "1000000") int count) {
        DataTest.reset();
        //多线程循环一定次数调用Data类不同实例的wrong方法
        IntStream.rangeClosed(1, count).parallel().forEach(i -> new DataTest().wrong());
        return DataTest.getCounter();
    }
}
