package com.abhisekhsite.Authentication.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.abhisekhsite.Authentication.model.EventMessage;
import com.abhisekhsite.Authentication.repo.RoomRepository;
import com.abhisekhsite.Authentication.service.KafkaProducerService;

@Controller
public class SocketController {
 
    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaProducerService kafkaProducer;
    private final RoomRepository roomRepository;

    public SocketController(SimpMessagingTemplate messagingTemplate, 
                           KafkaProducerService kafkaProducer,
                           RoomRepository roomRepository) {
        this.messagingTemplate = messagingTemplate;
        this.kafkaProducer = kafkaProducer;
        this.roomRepository = roomRepository;
    }
    
    @MessageMapping("/send")
    public void handleSend(@Payload EventMessage msg) {
        // Validate room exists
        if (msg.getRoom() == null || !roomRepository.existsByRoomId(msg.getRoom())) {
            System.err.println("Invalid room: " + msg.getRoom());
            return; // Silently ignore invalid room messages
        }
        
        if (msg.getTimestamp() == 0) msg.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/" + msg.getRoom(), msg);
        kafkaProducer.sendEvent(msg);
    }

    @MessageMapping("/file")
    public void handleFile(@Payload EventMessage msg) {
        // Validate room exists
        if (msg.getRoom() == null || !roomRepository.existsByRoomId(msg.getRoom())) {
            System.err.println("Invalid room: " + msg.getRoom());
            return; // Silently ignore invalid room messages
        }
        
        if (msg.getTimestamp() == 0) msg.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/" + msg.getRoom(), msg);
        kafkaProducer.sendEvent(msg);
    }
}
