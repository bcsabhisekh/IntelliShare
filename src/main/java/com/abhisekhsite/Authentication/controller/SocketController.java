package com.abhisekhsite.Authentication.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.abhisekhsite.Authentication.model.EventMessage;
import com.abhisekhsite.Authentication.service.KafkaProducerService;

@Controller
public class SocketController {
 
    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaProducerService kafkaProducer;

    public SocketController(SimpMessagingTemplate messagingTemplate, KafkaProducerService kafkaProducer) {
        this.messagingTemplate = messagingTemplate;
        this.kafkaProducer = kafkaProducer;
    }
    
    @MessageMapping("/send")
    public void handleSend(@Payload EventMessage msg) {
        if (msg.getTimestamp() == 0) msg.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/" + msg.getRoom(), msg);
        kafkaProducer.sendEvent(msg);
    }

    @MessageMapping("/file")
    public void handleFile(@Payload EventMessage msg) {
        if (msg.getTimestamp() == 0) msg.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/" + msg.getRoom(), msg);
        kafkaProducer.sendEvent(msg);
    }
}
