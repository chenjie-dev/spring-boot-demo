package com.example.ratelimit.controller;

import com.example.ratelimit.config.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @RateLimit(time = 1, count = 2)
    @GetMapping("/test")
    public String test() {
        return "Success!";
    }
} 