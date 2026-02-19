package com.abhisekhsite.Authentication.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;

import com.abhisekhsite.Authentication.model.EventMessage;
import com.abhisekhsite.Authentication.repo.RoomRepository;
import com.abhisekhsite.Authentication.service.KafkaProducerService;
import com.abhisekhsite.Authentication.service.S3Service;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

	
	private final S3Service s3Service;
	private final KafkaProducerService kafkaProducerService;
	private final RoomRepository roomRepository;
	
	
	public FileUploadController(S3Service s3Service, 
	                            KafkaProducerService kafkaProducerService,
	                            RoomRepository roomRepository) {
		this.s3Service = s3Service;
		this.kafkaProducerService = kafkaProducerService;
		this.roomRepository = roomRepository;
	}
	
	
	@PostMapping("/upload")
	public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
			@AuthenticationPrincipal User principal, 
			@RequestParam("room") String room) throws Exception{
		   String uploader = principal.getUsername();
		   
		   // Validate room exists
		   if (!roomRepository.existsByRoomId(room)) {
			   return ResponseEntity.badRequest()
				   .body(Map.of("error", "Room not found. Please create the room first."));
		   }
		   
		   String key = s3Service.uploadFile(file);
		   String url = s3Service.getFileUrl(key);
		   
		   EventMessage em = new EventMessage();
		   em.setRoom(room);
		   em.setContent(url);
		   em.setSender(uploader);
		   em.setType("file");
		   em.setTimestamp(System.currentTimeMillis());
		   
		   kafkaProducerService.sendEvent(em);
		   
		   return ResponseEntity.ok(Map.of("url", url, "key", key));
		   
	}
	
	
}
