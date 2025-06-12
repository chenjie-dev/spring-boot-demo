package com.chenjie.elasticsearch.config;

import com.alibaba.fastjson.JSONObject;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.net.ssl.SSLContext;
import java.util.concurrent.TimeUnit;

/**
 * @Description Elasticsearch账密认证模式的客户端配置
 */
@Configuration
@Slf4j
public class ESRestAuthConfig {

    @Value("${spring.elasticSearch.host}")
    private String[] esHost;
    @Value("${spring.elasticSearch.port}")
    private int esPort;
    @Value("${spring.elasticSearch.scheme:http}")
    private String scheme;
    @Value("${spring.elasticSearch.user.name:null}")
    private String esUserName;
    @Value("${spring.elasticSearch.user.password:null}")
    private String esUserPassword;
    @Value("${spring.elasticSearch.auth-enable:false}")
    private Boolean authEnable;
    /**
     * ES空闲连接保持时间，单位s，默认3s
     */
    @Value("${spring.elasticSearch.keepAliveTime:3}")
    private int keepAliveTime;

    private static HttpHost[] makeHttpHost(String[] host, int port, String scheme) {
        HttpHost[] hosts = new HttpHost[host.length];

        for (int i = 0; i < hosts.length; ++i) {
            hosts[i] = new HttpHost(host[i], port, scheme);
        }

        log.info("hosts组装成功：[{}]", JSONObject.toJSONString(hosts));
        return hosts;
    }

    /**
     * 走认证模式的客户端，注入时需要使用@Qualifier("AuthElasticsearchClient")进行指定
     *
     * @return
     */
    @Bean("AuthElasticsearchClient")
    public ElasticsearchClient createAuthElasticsearchClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(esUserName, esUserPassword));

        final SSLContext sslContext;
        try {
            sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();
        } catch (Exception e) {
            log.error("Failed to create SSL context", e);
            throw new RuntimeException("Failed to create SSL context", e);
        }

        // 创建 RestClient
        RestClient restClient = RestClient.builder(new HttpHost(esHost[0], esPort, scheme))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.setSSLContext(sslContext);
                    httpClientBuilder.setSSLHostnameVerifier((hostname, session) -> true);
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    httpClientBuilder.setKeepAliveStrategy((response, context) -> TimeUnit.SECONDS.toMillis(keepAliveTime));
                    httpClientBuilder.setMaxConnTotal(100);
                    httpClientBuilder.setMaxConnPerRoute(100);
                    return httpClientBuilder;
                })
                .setRequestConfigCallback(requestConfigBuilder -> {
                    requestConfigBuilder.setConnectTimeout(1000);
                    requestConfigBuilder.setSocketTimeout(30000);
                    requestConfigBuilder.setConnectionRequestTimeout(500);
                    return requestConfigBuilder;
                })
                .build();

        // 创建传输层
        ElasticsearchTransport transport = new RestClientTransport(
            restClient,
            new JacksonJsonpMapper()
        );

        // 创建客户端
        return new ElasticsearchClient(transport);
    }

    @Bean("noAuthElasticsearchClient")
    public ElasticsearchClient createNoAuthElasticsearchClient() {
        return getElasticsearchClient(esHost, esPort, scheme);
    }

    private void setMutiConnectConfig(RestClientBuilder restClientBuilder) {
        restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(100);
            httpClientBuilder.setMaxConnPerRoute(100);
            return httpClientBuilder;
        });
    }

    private void setConnectTimeOutConfig(RestClientBuilder restClientBuilder) {
        restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(1000);
            requestConfigBuilder.setSocketTimeout(30000);
            requestConfigBuilder.setConnectionRequestTimeout(500);
            return requestConfigBuilder;
        });
    }

    public ElasticsearchClient getElasticsearchClient(String[] host, int port, String scheme) {
        final HttpHost[] hosts = makeHttpHost(host, port, scheme);
        
        // 创建 RestClient
        RestClient restClient = RestClient.builder(hosts)
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.setKeepAliveStrategy((response, context) -> TimeUnit.SECONDS.toMillis(keepAliveTime));
                    httpClientBuilder.setMaxConnTotal(100);
                    httpClientBuilder.setMaxConnPerRoute(100);
                    return httpClientBuilder;
                })
                .setRequestConfigCallback(requestConfigBuilder -> {
                    requestConfigBuilder.setConnectTimeout(1000);
                    requestConfigBuilder.setSocketTimeout(30000);
                    requestConfigBuilder.setConnectionRequestTimeout(500);
                    return requestConfigBuilder;
                })
                .setFailureListener(new RestClient.FailureListener() {
                    public void onFailure(org.elasticsearch.client.Node node) {
                        log.error("elasticSearch - failure：[{}]", node.toString());
                    }
                })
                .build();

        // 创建传输层
        ElasticsearchTransport transport = new RestClientTransport(
            restClient,
            new JacksonJsonpMapper()
        );

        // 创建客户端
        return new ElasticsearchClient(transport);
    }

    @Bean("AutoCheckWhetherAuthElasticsearchClient")
    @Primary
    public ElasticsearchClient createAutoCheckWhetherAuthElasticsearchClient() {
        if (authEnable) {
            return createAuthElasticsearchClient();
        } else {
            return createNoAuthElasticsearchClient();
        }
    }
}
