package com.abhisekhsite.Authentication.service;


import java.util.ArrayList;
import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.abhisekhsite.Authentication.model.EventMessage;
import com.abhisekhsite.Authentication.model.FileRecord;
import com.abhisekhsite.Authentication.model.RoomMessage;
import com.abhisekhsite.Authentication.repo.FileRecordRepository;
import com.abhisekhsite.Authentication.repo.RoomMessageRepository;

@Component
public class KafkaBatchConsumer {

	   private final RoomMessageRepository messageRepo;
	   private final FileRecordRepository fileRepo;
	   private final AIService aiService;
	   private final RedisService redisService;
	   
	   private final List<EventMessage> buffer = new ArrayList<>();

	   public KafkaBatchConsumer(RoomMessageRepository messageRepo, 
	                            FileRecordRepository fileRepo,
	                            AIService aiService,
	                            RedisService redisService) {
		this.messageRepo = messageRepo;
		this.fileRepo = fileRepo;
		this.aiService = aiService;
		this.redisService = redisService;
	   }
	   
	   @KafkaListener(topics = "room-events", groupId = "room-events-group"
			   , containerFactory = "kafkaListenerContainerFactory") 
	   public void consume(EventMessage msg) {
		   synchronized (buffer) {
			   buffer.add(msg);
		   }
	   }
	   
	   
	   @Scheduled(fixedRate = 10000)
	   public void flushBufferToDb() {
		   List<EventMessage> toProcess;
		   synchronized (buffer) {
           if(buffer.isEmpty())  return;
           toProcess = new ArrayList<>(buffer);
           buffer.clear();
		}
		   
		   List<RoomMessage> messageToSave = new ArrayList<>();
		   List<FileRecord> filesToSave = new ArrayList<>();
		   
		   for(EventMessage e: toProcess) {
			   if("chat".equalsIgnoreCase(e.getType())) {
				   RoomMessage rm = new RoomMessage();
				   rm.setRoom(e.getRoom());
				   rm.setSender(e.getSender());
				   rm.setContent(e.getContent());
				   rm.setType("chat");
				   
				   // Score message importance using AI
				   double importanceScore = aiService.scoreMessageImportance(e.getContent(), e.getSender());
				   rm.setImportanceScore(importanceScore);
				   
				   messageToSave.add(rm);
			   } else if("file".equalsIgnoreCase(e.getType())) {
				   FileRecord fr = new FileRecord();
				   fr.setRoom(e.getRoom());
				   fr.setUploader(e.getSender());
				   fr.setUrl(e.getContent());
				   filesToSave.add(fr);
			   }
		   }
		   
		   if(!messageToSave.isEmpty()) {
			   List<RoomMessage> savedMessages = messageRepo.saveAll(messageToSave);
			   
			   // Cache messages in Redis grouped by room
			   savedMessages.stream()
			       .collect(java.util.stream.Collectors.groupingBy(RoomMessage::getRoom))
			       .forEach((room, messages) -> {
			           for (RoomMessage msg : messages) {
			               redisService.addMessageToCache(room, msg);
			           }
			       });
		   }
		   
		   if(!filesToSave.isEmpty()) {
			   fileRepo.saveAll(filesToSave);
		   }
	   }
	   
}
