package com.example.spring_redis_kafka.service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    @KafkaListener(topics = "defaultTopic", groupId = "foo")
    public void sub(String message) {
        System.out.println(String.format("Subscribed :  %s", message));
    }
}
