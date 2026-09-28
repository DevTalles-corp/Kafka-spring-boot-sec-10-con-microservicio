package com.bistro.notifications.config;

import com.bistro.notifications.shared.NonRetryableException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.kafka.autoconfigure.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
public class KafkaErrorHandlingConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate){

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> {
                    log.warn("A DLT: topic={}, particion={}, offset={}, key={}, causa={}",
                            record.topic(), record.partition(), record.offset(),
                            record.key(), exception.getMessage());
                    return new TopicPartition(record.topic() + "-dlt", record.partition());
                }
        );


        FixedBackOff backOff = new FixedBackOff(2000L, 3);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.addNotRetryableExceptions(NonRetryableException.class);

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> dltReprocessorFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        configurer.configure(factory, consumerFactory);

        // Sin reintentos y sin DLT: si el reprocesador no puede leer un mensaje, lo saltea
        factory.setCommonErrorHandler(
                new DefaultErrorHandler(
                (record, exception) -> log.warn(
                        "Salteado por el reprocesador (ilegible): topic={}, key={}, causa={}",
                        record.topic(), record.key(), exception.getMessage()),
                new FixedBackOff(0L, 0L)));

        return factory;
    }

    @Bean
    public NewTopic reservationConfirmedV2DltTopic() {
        return TopicBuilder.name("reservation-confirmed-v2-dlt")
                .partitions(3)
                .replicas(1).build();
    }

    @Bean
    public NewTopic reservationRejectedDltTopic() {
        return TopicBuilder.name("reservation-rejected-dlt").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic reservationCancelledDltTopic() {
        return TopicBuilder.name("reservation-cancelled-dlt").partitions(3).replicas(1).build();
    }
}











