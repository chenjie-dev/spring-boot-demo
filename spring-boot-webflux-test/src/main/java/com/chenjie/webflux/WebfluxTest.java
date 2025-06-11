package com.chenjie.webflux;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class WebfluxTest {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String httpUrlGet = "https://tool.lu/article/report";
    private static final String httpUrlPost = "localhost:10001/test/post";

    @Test
    public void testWebClientGet() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);

        Mono<String> mono = webClientBuilder
                .baseUrl(httpUrlGet)
                .build()
                .get()
                .retrieve()
                .bodyToMono(String.class);

        mono.doOnError(e -> {
            log.warn(">>>>>  exception:{} >>>>>", e.getMessage());
        }).subscribe(res -> {
//            调用subscribe方法订阅Mono对象，并在收到响应时记录信息日志
            log.info(">>>>> subscribe <<<<<");
            log.info(">>>>> res:{} <<<<<", counter.getAndIncrement());
        });
        log.info(">>>>> 主线程结束 <<<<<");

        Thread.sleep(100 * 1000L);
    }

    @Test
    public void testWebClientPost() {

        Mono<String> mono = webClientBuilder
                .baseUrl(httpUrlPost)
                .build()
                .post()
                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(typeDto)
                .retrieve()
                .bodyToMono(String.class);

        String result = mono.block();

        System.out.println("执行了！！！！！");
    }
}
