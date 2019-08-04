package com.space.test.controller;

import com.space.core.MyController;
import com.space.core.MyRequestMapping;

/**
 * Created by lucifel on 19-8-3.
 */
@MyController
public class HandlerTest2 {

    @MyRequestMapping("/hello")
    public String hello(){
        return "index2";
    }

}
