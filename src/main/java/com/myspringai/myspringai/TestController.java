package com.myspringai.myspringai;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello")
    public String test(){
        return "Hello World"; //This is a test
    }
}
