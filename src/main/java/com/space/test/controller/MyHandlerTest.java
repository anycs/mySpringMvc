package com.space.test.controller;

import com.space.core.MyController;
import com.space.core.MyRequestMapping;

/**
 * Created by lucifel on 19-8-3.
 */
@MyController
@MyRequestMapping("/test")
public class MyHandlerTest {

    @MyRequestMapping("/hello")
    public String hello(){
        return "redirect:/hello";
    }

    @MyRequestMapping("/hello2")
    public String hello2(){
        return "forward:/test/hello3";
    }

    @MyRequestMapping("/hello3")
    public String hello3(){
        return "index";
    }


}
