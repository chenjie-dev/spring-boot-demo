package com.chenjie.kafka.service;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.chenjie.kafka.constants.KafkaPropertyConstants.*;


/**
 * kafka使用封装
 */
@SuppressWarnings({"unchecked"})
public final class KafkaService<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);
    private static final String DEFAULT_SEPARATOR = "_";
    private KafkaTemplate<K, V> kafkaTemplate;
    private KafkaTemplate<K, V> aaKafkaTemplate;
    private String namespace;
    private Environment env;
    private KubernetesClient kubernetesClient;
    private ExecutorService workers;

    public KafkaService(KafkaTemplate<K, V> kafkaTemplate, Environment env, KubernetesClient client) {
        this.kafkaTemplate = kafkaTemplate;
        this.env = env;
        this.kubernetesClient = client;
        String namespace = env.getProperty(SPRING_KAFKA_PRODUCER_NAMESPACE_KEY);
        if (Objects.isNull(namespace)) {
            setDefaultNamespace();
        } else {
            setNamespace(namespace);
        }
        logger.info("[ProducerConfig] Specify namespace as {}", this.namespace.equals(DEFAULT_SPRING_KAFKA_NAMESPACE) ? "EMPTY STRING" : this.namespace);
        int poolSize = Integer.parseInt(env.getProperty(SPRING_KAFKA_PRODUCER_THREADS_SIZE, "10"));
        this.workers = Executors.newFixedThreadPool(poolSize);
    }

    public KafkaService(KafkaTemplate<K, V> kafkaTemplate, KafkaTemplate<K, V> aaKafkaTemplate, Environment env, KubernetesClient client) {
        this.kafkaTemplate = kafkaTemplate;
        this.aaKafkaTemplate = aaKafkaTemplate;
        this.env = env;
        this.kubernetesClient = client;
        String namespace = env.getProperty(SPRING_KAFKA_PRODUCER_NAMESPACE_KEY);
        if (Objects.isNull(namespace)) {
            setDefaultNamespace();
        } else {
            setNamespace(namespace);
        }
        logger.info("[ProducerConfig] Specify namespace as {}", this.namespace.equals(DEFAULT_SPRING_KAFKA_NAMESPACE) ? "EMPTY STRING" : this.namespace);
        int poolSize = Integer.parseInt(env.getProperty(SPRING_KAFKA_PRODUCER_THREADS_SIZE, "10"));
        this.workers = Executors.newFixedThreadPool(poolSize);
    }

    /**
     * 为topic添加公共命名空间前缀
     * e.g. topic = myTopic, then the result is {@code public_myTopic}
     *
     * @param topic 原始topic
     * @return prefixedTopic
     */
    public static String getPublicNamespaceKey(String topic) {
        return getCustomPublicNamespaceKey(topic, null);
    }

    /**
     * 为topic添加公共命名空间前缀
     * e.g. topic = myTopic, groups = [test1,test2], then the result is {@code public_test1_test2_myTopic}
     *
     * @param topic  原始topic
     * @param groups 前缀组
     * @return prefixedTopic
     */
    public static String getCustomPublicNamespaceKey(String topic, @Nullable List<String> groups) {
        if (Objects.isNull(groups)) {
            return PUBLIC_NAMESPACE_PREFIX + DEFAULT_SEPARATOR + topic;
        }
        StringBuilder stringBuilder = new StringBuilder(PUBLIC_NAMESPACE_PREFIX + DEFAULT_SEPARATOR);
        groups.forEach(e -> {
            stringBuilder.append(e).append(DEFAULT_SEPARATOR);
        });
        stringBuilder.append(topic).append(DEFAULT_SEPARATOR);
        return stringBuilder.toString();
    }

    public KafkaTemplate<K, V> getKafkaTemplate() {
        return kafkaTemplate;
    }

    public void setKafkaTemplate(KafkaTemplate<K, V> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 发送消息到指定topic
     *
     * @param topic
     * @param data
     * @return
     */
    public Future<CompletableFuture<SendResult<K, V>>> send(String topic, V data) {
        if (Objects.isNull(kafkaTemplate)) {
            logger.error("Cannot find the bean kafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> kafkaTemplate.send(addTopicPrefix(topic), data));
    }

    public Future<CompletableFuture<SendResult<K, V>>> send(String topic, String namespace, V data) {
        if (Objects.isNull(kafkaTemplate)) {
            logger.error("Cannot find the bean kafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> kafkaTemplate.send(addTopicPrefix(topic, namespace), data));
    }

    /**
     * 发送消息到指定topic，同时指定消息key
     *
     * @param topic
     * @param key
     * @param data
     * @return
     */
    public Future<CompletableFuture<SendResult<K, V>>> send(String topic, K key, @Nullable V data) {
        if (Objects.isNull(kafkaTemplate)) {
            logger.error("Cannot find the bean kafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> kafkaTemplate.send(addTopicPrefix(topic), key, data));
    }

    public Future<CompletableFuture<SendResult<K, V>>> send(String topic, String namespace, K key, @Nullable V data) {
        if (Objects.isNull(kafkaTemplate)) {
            logger.error("Cannot find the bean kafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> kafkaTemplate.send(addTopicPrefix(topic, namespace), key, data));
    }

    public Future<CompletableFuture<SendResult<K, V>>> send(String topic, Integer partition, K key, @Nullable V data) {
        if (Objects.isNull(kafkaTemplate)) {
            logger.error("Cannot find the bean kafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> kafkaTemplate.send(addTopicPrefix(topic), partition, key, data));
    }

    /**
     * 发送消息到指定topic的指定分区，同时指定消息key
     *
     * @param topic
     * @param namespace
     * @param partition
     * @param key
     * @param data
     * @return
     */
    public Future<CompletableFuture<SendResult<K, V>>> send(String topic, String namespace, Integer partition, K key, @Nullable V data) {
        if (Objects.isNull(kafkaTemplate)) {
            logger.error("Cannot find the bean kafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> kafkaTemplate.send(addTopicPrefix(topic, namespace), partition, key, data));
    }

    public Future<CompletableFuture<SendResult<K, V>>> send(String topic, Integer partition, Long timestamp, K key,
                                                           @Nullable V data) {
        if (Objects.isNull(kafkaTemplate)) {
            logger.error("Cannot find the bean kafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> kafkaTemplate.send(addTopicPrefix(topic), partition, timestamp, key, data));
    }

    /**
     * 发送消息到指定namespace的topic的指定分区中，同时指定消息key和消息时间
     *
     * @param topic
     * @param namespace
     * @param partition
     * @param timestamp
     * @param key
     * @param data
     * @return
     */
    public Future<CompletableFuture<SendResult<K, V>>> send(String topic, String namespace, Integer partition, Long timestamp, K key,
                                                           @Nullable V data) {
        if (Objects.isNull(kafkaTemplate)) {
            logger.error("Cannot find the bean kafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> kafkaTemplate.send(addTopicPrefix(topic, namespace), partition, timestamp, key, data));
    }

    /**
     * aaSend方法和send方法对应，只不过发到的是另外一个双活kafka
     *
     * @param topic
     * @param data
     * @return
     */
    public Future<CompletableFuture<SendResult<K, V>>> aaSend(String topic, V data) {
        if (Objects.isNull(aaKafkaTemplate)) {
            logger.error("Cannot find the bean aaKafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> aaKafkaTemplate.send(addTopicPrefix(topic), data));
    }

    public Future<CompletableFuture<SendResult<K, V>>> aaSend(String topic, String namespace, V data) {
        if (Objects.isNull(aaKafkaTemplate)) {
            logger.error("Cannot find the bean aaKafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> aaKafkaTemplate.send(addTopicPrefix(topic, namespace), data));
    }

    public Future<CompletableFuture<SendResult<K, V>>> aaSend(String topic, K key, @Nullable V data) {
        if (Objects.isNull(aaKafkaTemplate)) {
            logger.error("Cannot find the bean aaKafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> aaKafkaTemplate.send(addTopicPrefix(topic), key, data));
    }

    public Future<CompletableFuture<SendResult<K, V>>> aaSend(String topic, String namespace, K key, @Nullable V data) {
        if (Objects.isNull(aaKafkaTemplate)) {
            logger.error("Cannot find the bean aaKafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> aaKafkaTemplate.send(addTopicPrefix(topic, namespace), key, data));
    }

    public Future<CompletableFuture<SendResult<K, V>>> aaSend(String topic, Integer partition, K key, @Nullable V data) {
        if (Objects.isNull(aaKafkaTemplate)) {
            logger.error("Cannot find the bean aaKafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> aaKafkaTemplate.send(addTopicPrefix(topic), partition, key, data));
    }

    public Future<CompletableFuture<SendResult<K, V>>> aaSend(String topic, String namespace, Integer partition, K key, @Nullable V data) {
        if (Objects.isNull(aaKafkaTemplate)) {
            logger.error("Cannot find the bean aaKafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> aaKafkaTemplate.send(addTopicPrefix(topic, namespace), partition, key, data));
    }

    public Future<CompletableFuture<SendResult<K, V>>> aaSend(String topic, Integer partition, Long timestamp, K key,
                                                             @Nullable V data) {
        if (Objects.isNull(aaKafkaTemplate)) {
            logger.error("Cannot find the bean aaKafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> aaKafkaTemplate.send(addTopicPrefix(topic), partition, timestamp, key, data));
    }

    public Future<CompletableFuture<SendResult<K, V>>> aaSend(String topic, String namespace, Integer partition, Long timestamp, K key,
                                                             @Nullable V data) {
        if (Objects.isNull(aaKafkaTemplate)) {
            logger.error("Cannot find the bean aaKafkaTemplate, please check your configuration.");
            return null;
        }
        return workers.submit(() -> aaKafkaTemplate.send(addTopicPrefix(topic, namespace), partition, timestamp, key, data));
    }

    /**
     * 同时发送
     *
     * @param topic 发送的主题
     * @param data  发送的内容
     */
    public List<Future<CompletableFuture<SendResult<K, V>>>> bothSend(String topic, V data) {
        return Arrays.asList(send(topic, data), aaSend(topic, data));
    }

    public List<Future<CompletableFuture<SendResult<K, V>>>> bothSend(String topic, String namespace, V data) {
        return Arrays.asList(send(topic, namespace, data), aaSend(topic, namespace, data));
    }

    public List<Future<CompletableFuture<SendResult<K, V>>>> bothSend(String topic, K key, @Nullable V data) {
        return Arrays.asList(send(topic, key, data), aaSend(topic, key, data));
    }

    public List<Future<CompletableFuture<SendResult<K, V>>>> bothSend(String topic, String namespace, K key, @Nullable V data) {
        return Arrays.asList(send(topic, namespace, key, data), aaSend(topic, namespace, key, data));
    }

    public List<Future<CompletableFuture<SendResult<K, V>>>> bothSend(String topic, Integer partition, K key, @Nullable V data) {
        return Arrays.asList(send(topic, partition, key, data), aaSend(topic, partition, key, data));
    }

    public List<Future<CompletableFuture<SendResult<K, V>>>> bothSend(String topic, String namespace, Integer partition, K key, @Nullable V data) {
        return Arrays.asList(send(topic, namespace, partition, key, data), aaSend(topic, namespace, partition, key, data));
    }

    public List<Future<CompletableFuture<SendResult<K, V>>>> bothSend(String topic, Integer partition, Long timestamp, K key,
                                                                     @Nullable V data) {
        return Arrays.asList(send(topic, partition, timestamp, key, data), aaSend(topic, partition, timestamp, key, data));
    }

    public List<Future<CompletableFuture<SendResult<K, V>>>> bothSend(String topic, String namespace, Integer partition, Long timestamp, K key,
                                                                     @Nullable V data) {
        return Arrays.asList(send(topic, namespace, partition, timestamp, key, data), aaSend(topic, namespace, partition, timestamp, key, data));
    }

    private String addTopicPrefix(String topic) {
        return "".equals(namespace) ? topic : namespace + DEFAULT_SEPARATOR + topic;
    }

    private String addTopicPrefix(String topic, String namespace) {
        return "".equals(namespace) ? topic : namespace + DEFAULT_SEPARATOR + topic;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    private void setDefaultNamespace() {
        String k8sEnabled = this.env.getProperty("spring.cloud.kubernetes.enabled", Boolean.TRUE + "");
        if ((Boolean.TRUE + "").equalsIgnoreCase(k8sEnabled)) {
            this.setNamespace(kubernetesClient.getNamespace() == null ? DEFAULT_SPRING_KAFKA_NAMESPACE : kubernetesClient.getNamespace());
        } else {
            this.setNamespace(this.env.getProperty(SPRING_KAFKA_PRODUCER_NAMESPACE_KEY, DEFAULT_SPRING_KAFKA_NAMESPACE));
        }
    }
}
