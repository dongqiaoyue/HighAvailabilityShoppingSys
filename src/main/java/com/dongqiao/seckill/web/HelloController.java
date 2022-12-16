package com.dongqiao.seckill.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @description: hello
 * @author: Dongqiao Yue
 * @create: 2022-12-15 22:24
 **/

@Controller
public class HelloController {

//    @ResponseBody
    @RequestMapping("/hi")
    public String helloHandler(Map<String, Object> resultMap) {
        resultMap.put("info", "hello");
        return "hello";
    }
}
