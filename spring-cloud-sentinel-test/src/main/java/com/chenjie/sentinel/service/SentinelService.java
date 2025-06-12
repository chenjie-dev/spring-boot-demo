package com.chenjie.sentinel.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.stereotype.Service;

@Service
public class SentinelService {

    @SentinelResource(value = "hello", blockHandler = "handleBlock", fallback = "handleFallback")
    public String hello(String name) {
        return "Hello, " + name;
    }

    public String handleBlock(String name, BlockException ex) {
        return "Blocked: " + name;
    }

    public String handleFallback(String name, Throwable ex) {
        return "Fallback: " + name;
    }
} 