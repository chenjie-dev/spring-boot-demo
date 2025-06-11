package com.chenjie.cloud.config;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.util.function.Function;

/**
 * webclient 相关优化配置
 */
@Component
@Slf4j
public class WebClientConfig {

    @Value("${webclient.connection:-1}")
    public int connection;
    @Value("${webclient.worker:4}")
    public int worker;
    @Value("${webclient.connect-timeout:6000}")
    public int connectTimeout;

    @Bean
    public ReactorResourceFactory reactorResourceFactory() {
        ReactorResourceFactory factory = new ReactorResourceFactory();
        //  默认情况下，WebClient使用global Reactor Netty资源
        //  禁止使用全局资源
        factory.setUseGlobalResources(false);
        if (connection <= 0) {
            factory.setConnectionProvider(ConnectionProvider.builder("httpClient").build());
            log.info("webclient elastic config worker : {} , connection : {}", worker, connection);
        } else {
            factory.setConnectionProvider(ConnectionProvider.create("httpClient", connection));
            log.info("webclient fixed config worker : {} , connection : {}", worker, connection);
        }
        // 设置连接数

        // 设置 worker 数量
        factory.setLoopResources(LoopResources.create("httpClient", worker, true));

        return factory;
    }

    @Bean

    public WebClient webClient(ReactorResourceFactory reactorResourceFactory) {

        Function<HttpClient, HttpClient> mapper = httpClient ->
                httpClient.tcpConfiguration(c -> c.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout));

        ClientHttpConnector connector = new ReactorClientHttpConnector(reactorResourceFactory, mapper);

        return WebClient.builder().clientConnector(connector).build();
    }
}
