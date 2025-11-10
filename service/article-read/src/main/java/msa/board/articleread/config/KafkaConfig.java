package msa.board.articleread.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
public class KafkaConfig {
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
        ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        /**
         * Kafka 메시지를 수신한 후 직접 오프셋을 커밋(Ack)
         * RECORD	각 메시지를 처리할 때마다 자동 커밋
         * BATCH	폴링 단위(batch)로 자동 커밋
         * TIME 	일정 시간 간격마다 자동 커밋
         * COUNT	일정 개수마다 자동 커밋
         * MANUAL_IMMEDIATE	명시적 ack 호출 즉시 커밋
         * MANUAL	ack 호출 시점 이후 다음 폴링 전에 커밋
         */
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
