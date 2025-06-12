package com.chenjie.kafka;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class KafkaTestProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String testTopic = "chenjie-test-topic";

    @Test
    public void test() {
        while (true) {
            kafkaTemplate.send(testTopic, "test" + System.currentTimeMillis())
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info(">>>>> 发送成功 <<<<<");
                        } else {
                            log.info(">>>>> 发送失败 <<<<<");
                        }
                    });
        }
    }
}