package com.chenjie.kafka.autoconfigure;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.security.jaas.KafkaJaasLoginModuleInitializer;
import org.springframework.kafka.support.LoggingProducerListener;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.transaction.KafkaTransactionManager;

import java.io.IOException;

/**
 * 双活kafka配置
 */
@Configuration
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
@EnableConfigurationProperties({AAKafkaProperties.class, KafkaProperties.class})
public class ActiveActiveKafkaConfigure {
    private final AAKafkaProperties aaKafkaProperties;
    private final KafkaProperties kafkaProperties;

    public ActiveActiveKafkaConfigure(KafkaProperties kafkaProperties, AAKafkaProperties aaKafkaProperties) {
        this.aaKafkaProperties = aaKafkaProperties;
        this.kafkaProperties = kafkaProperties;
    }

    @Bean("aaKafkaTemplate")
    @ConditionalOnProperty(name = "spring.aa-kafka.enabled", havingValue = "true")
    public KafkaTemplate<?, ?> aaKafkaTemplate(ProducerFactory<Object, Object> aaKafkaProducerFactory,
                                               ProducerListener<Object, Object> aaKafkaProducerListener,
                                               ObjectProvider<RecordMessageConverter> messageConverter) {
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(aaKafkaProducerFactory);
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        kafkaTemplate.setProducerListener(aaKafkaProducerListener);
        kafkaTemplate.setDefaultTopic(this.aaKafkaProperties.getTemplate().getDefaultTopic());
        return kafkaTemplate;
    }

    @Bean("aaKafkaProducerListener")
    @ConditionalOnProperty(name = "spring.aa-kafka.enabled", havingValue = "true")
    public ProducerListener<Object, Object> aaKafkaProducerListener() {
        return new LoggingProducerListener<>();
    }

    @Bean("aaKafkaConsumerFactory")
    @ConditionalOnProperty(name = "spring.aa-kafka.enabled", havingValue = "true")
    public ConsumerFactory<?, ?> aaKafkaConsumerFactory(
            ObjectProvider<DefaultKafkaConsumerFactoryCustomizer> customizers) {
        DefaultKafkaConsumerFactory<Object, Object> factory = new DefaultKafkaConsumerFactory<>(
                this.aaKafkaProperties.buildConsumerProperties());
        customizers.orderedStream().forEach((customizer) -> customizer.customize(factory));
        return factory;
    }

    @Bean("aaKafkaProducerFactory")
    @ConditionalOnProperty(name = "spring.aa-kafka.enabled", havingValue = "true")
    public ProducerFactory<?, ?> aaKafkaProducerFactory(
            ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers) {
        DefaultKafkaProducerFactory<?, ?> factory = new DefaultKafkaProducerFactory<>(
                this.aaKafkaProperties.buildProducerProperties());
        String transactionIdPrefix = this.aaKafkaProperties.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        customizers.orderedStream().forEach((customizer) -> customizer.customize(factory));
        return factory;
    }

    /**
     * 使用@KafkaListener时指定containerFactory为authListenerContainerFactory
     *
     * @return
     */
    @Bean("aaListenerContainerFactory")
    @ConditionalOnProperty(name = "spring.aa-kafka.enabled", havingValue = "true")
    public ConcurrentKafkaListenerContainerFactory listenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer) {
        //指定使用DefaultKafkaConsumerFactory
        DefaultKafkaConsumerFactory<Object, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(this.aaKafkaProperties.buildConsumerProperties());
        ConcurrentKafkaListenerContainerFactory factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory);
        configurer.configure(factory, consumerFactory);
        return factory;
    }

    @Bean("aaKafkaTransactionManager")
    @ConditionalOnProperty(name = "spring.aa-kafka.producer.transaction-id-prefix")
    public KafkaTransactionManager<?, ?> aaKafkaTransactionManager(ProducerFactory<?, ?> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }

    @Bean("aaKafkaJaasInitializer")
    @ConditionalOnProperty(name = "spring.aa-kafka.jaas.enabled")
    public KafkaJaasLoginModuleInitializer aaKafkaJaasInitializer() throws IOException {
        KafkaJaasLoginModuleInitializer jaas = new KafkaJaasLoginModuleInitializer();
        AAKafkaProperties.Jaas jaasProperties = this.aaKafkaProperties.getJaas();
        if (jaasProperties.getControlFlag() != null) {
            jaas.setControlFlag(jaasProperties.getControlFlag());
        }
        if (jaasProperties.getLoginModule() != null) {
            jaas.setLoginModule(jaasProperties.getLoginModule());
        }
        jaas.setOptions(jaasProperties.getOptions());
        return jaas;
    }

    @Bean("aaKafkaAdmin")
    @ConditionalOnProperty(name = "spring.aa-kafka.enabled", havingValue = "true")
    public KafkaAdmin aaKafkaAdmin() {
        KafkaAdmin kafkaAdmin = new KafkaAdmin(this.aaKafkaProperties.buildAdminProperties());
        kafkaAdmin.setFatalIfBrokerNotAvailable(this.aaKafkaProperties.getAdmin().isFailFast());
        return kafkaAdmin;
    }

    //---------------------------------------------

    @Bean("kafkaTemplate")
    public KafkaTemplate<?, ?> kafkaTemplate(ProducerFactory<Object, Object> kafkaProducerFactory,
                                             ProducerListener<Object, Object> kafkaProducerListener,
                                             ObjectProvider<RecordMessageConverter> messageConverter) {
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(kafkaProducerFactory);
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        kafkaTemplate.setProducerListener(kafkaProducerListener);
        kafkaTemplate.setDefaultTopic(this.kafkaProperties.getTemplate().getDefaultTopic());
        return kafkaTemplate;
    }

    @Bean
    public ProducerListener<Object, Object> kafkaProducerListener() {
        return new LoggingProducerListener<>();
    }

    @Bean
    public ConsumerFactory<?, ?> kafkaConsumerFactory(
            ObjectProvider<DefaultKafkaConsumerFactoryCustomizer> customizers) {
        DefaultKafkaConsumerFactory<Object, Object> factory = new DefaultKafkaConsumerFactory<>(
                this.kafkaProperties.buildConsumerProperties());
        customizers.orderedStream().forEach((customizer) -> customizer.customize(factory));
        return factory;
    }

    @Bean
    public ProducerFactory<?, ?> kafkaProducerFactory(
            ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers) {
        DefaultKafkaProducerFactory<?, ?> factory = new DefaultKafkaProducerFactory<>(
                this.kafkaProperties.buildProducerProperties());
        String transactionIdPrefix = this.kafkaProperties.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        customizers.orderedStream().forEach((customizer) -> customizer.customize(factory));
        return factory;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.producer.transaction-id-prefix")
    public KafkaTransactionManager<?, ?> kafkaTransactionManager(ProducerFactory<?, ?> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.jaas.enabled")
    public KafkaJaasLoginModuleInitializer kafkaJaasInitializer() throws IOException {
        KafkaJaasLoginModuleInitializer jaas = new KafkaJaasLoginModuleInitializer();
        KafkaProperties.Jaas jaasProperties = this.kafkaProperties.getJaas();
        if (jaasProperties.getControlFlag() != null) {
            jaas.setControlFlag(jaasProperties.getControlFlag());
        }
        if (jaasProperties.getLoginModule() != null) {
            jaas.setLoginModule(jaasProperties.getLoginModule());
        }
        jaas.setOptions(jaasProperties.getOptions());
        return jaas;
    }
}
