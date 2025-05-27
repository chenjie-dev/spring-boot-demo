package com.chenjie.redis.config;

import com.chenjie.redis.StarterRedisTemplate;
import com.chenjie.redis.constant.RedisStarterConstant;
import com.chenjie.redis.serializer.RedisKeySerializer;
import com.chenjie.redis.serializer.RedisValueJacksonSerializer;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.resource.ClientResources;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * @Description redis自动配置类
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(PrefixConfig.class)
public class RedisServiceAutoConfigure {

    @Autowired
    private RedisProperties redisProperties;

    @Autowired
    private PrefixConfig prefixConfig;

    /**
     * key 的序列化器
     */
    private RedisKeySerializer keyRedisSerializer;

    /**
     * value 的序列化器
     */
    private RedisValueJacksonSerializer valueRedisSerializer = new RedisValueJacksonSerializer();

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // RedisCacheWriter
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
        // RedisCacheConfiguration - 值的序列化方式
        RedisSerializationContext.SerializationPair<Object> serializationPair = RedisSerializationContext.SerializationPair.fromSerializer(valueRedisSerializer);
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(serializationPair);

        return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration);
    }

    private GenericObjectPoolConfig getPoolConfig(RedisProperties.Pool properties) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(properties.getMaxActive());
        config.setMaxIdle(properties.getMaxIdle());
        config.setMinIdle(properties.getMinIdle());
        if (properties.getMaxWait() != null) {
            config.setMaxWaitMillis(properties.getMaxWait().toMillis());
        }
        return config;
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration clientConfig = getLettuceClientConfiguration();
        
        if (redisProperties.getCluster() != null && !redisProperties.getCluster().getNodes().isEmpty()) {
            // 集群模式
            RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(redisProperties.getCluster().getNodes());
            if (redisProperties.getPassword() != null) {
                clusterConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));
            }
            clusterConfig.setMaxRedirects(redisProperties.getCluster().getMaxRedirects());
            return new LettuceConnectionFactory(clusterConfig, clientConfig);
        } else {
            // 单机模式
            RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
            standaloneConfig.setHostName(redisProperties.getHost());
            standaloneConfig.setPort(redisProperties.getPort());
            if (redisProperties.getPassword() != null) {
                standaloneConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));
            }
            standaloneConfig.setDatabase(redisProperties.getDatabase());
            return new LettuceConnectionFactory(standaloneConfig, clientConfig);
        }
    }

    private LettuceClientConfiguration getLettuceClientConfiguration() {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder;
        
        // 设置连接池
        if (redisProperties.getLettuce() != null && redisProperties.getLettuce().getPool() != null) {
            builder = LettucePoolingClientConfiguration.builder()
                    .poolConfig(getPoolConfig(redisProperties.getLettuce().getPool()));
        } else {
            builder = LettuceClientConfiguration.builder();
        }
        
        // 设置超时时间
        if (redisProperties.getTimeout() != null) {
            builder.commandTimeout(redisProperties.getTimeout());
        }

        // 设置集群拓扑刷新选项
        if (redisProperties.getCluster() != null) {
            ClusterTopologyRefreshOptions clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                    .enableAllAdaptiveRefreshTriggers()
                    .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(10))
                    .build();

            ClusterClientOptions clusterClientOptions = ClusterClientOptions.builder()
                    .topologyRefreshOptions(clusterTopologyRefreshOptions)
                    .build();
            builder.clientOptions(clusterClientOptions);
        }

        return builder.build();
    }

    @Bean
    public StarterRedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StarterRedisTemplate<String, Object> starterRedisTemplate = new StarterRedisTemplate<>();

        // 配置连接工厂
        starterRedisTemplate.setConnectionFactory(redisConnectionFactory);
        // 值序列化-RedisFastJsonSerializer
        starterRedisTemplate.setValueSerializer(valueRedisSerializer);
        starterRedisTemplate.setHashValueSerializer(valueRedisSerializer);
        // 键序列化-StringRedisSerializer
        String localNamespace = chooseLocalNamespace();
        Boolean namespaceSwitch = prefixConfig.getNamespaceSwitch();
        this.keyRedisSerializer = new RedisKeySerializer(localNamespace, RedisStarterConstant.publicNamespace, namespaceSwitch);
        log.info("RedisStarter  ====>  localNamespace: {}   publicNamespace: {}", localNamespace, RedisStarterConstant.publicNamespace);
        starterRedisTemplate.setKeySerializer(keyRedisSerializer);
        starterRedisTemplate.setHashKeySerializer(keyRedisSerializer);

        return starterRedisTemplate;
    }

    private String chooseLocalNamespace() {
        // 获取配置文件中配的localNamespace
        String localNamespace = prefixConfig.getLocalNamespace();
        // 配置文件里配的localNamespace优先级最高
        if (localNamespace != null && !"".equals(localNamespace)) {
            return localNamespace;
        }
        return "local";
    }
}
