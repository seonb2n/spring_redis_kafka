package com.example.spring_redis_kafka.controller;

import com.example.spring_redis_kafka.service.kafka.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("/kafka/producer")
public class ProducerController {

    @Autowired
    private ProducerService producer;

    @PostMapping("/message")
    public void PublishMessage(@RequestParam String msg) {
        producer.pub(msg);
    }
}
