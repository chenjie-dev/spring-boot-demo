package com.chenjie.sentinel;

import com.chenjie.sentinel.service.SentinelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SentinelTest {

    @Autowired
    private SentinelService sentinelService;

    @Test
    void testHello() {
        String result = sentinelService.hello("World");
        assertEquals("Hello, World", result);
    }
} 