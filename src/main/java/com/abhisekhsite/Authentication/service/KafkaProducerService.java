package com.abhisekhsite.Authentication.service;

import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import com.abhisekhsite.Authentication.model.EventMessage;

@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, EventMessage> kafkaTemplate;
    public KafkaProducerService(KafkaTemplate<String, EventMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void sendEvent(EventMessage message) {
        kafkaTemplate.send("room-events", message.getRoom(), message);
    }
}
