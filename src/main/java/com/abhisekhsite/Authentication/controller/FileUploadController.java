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
import com.abhisekhsite.Authentication.service.KafkaProducerService;
import com.abhisekhsite.Authentication.service.S3Service;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

	
	private final S3Service s3Service;
	private final KafkaProducerService kafkaProducerService;
	
	
	public FileUploadController(S3Service s3Service, KafkaProducerService kafkaProducerService) {
		this.s3Service = s3Service;
		this.kafkaProducerService = kafkaProducerService;
	}
	
	
	@PostMapping("/upload")
	public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
			@AuthenticationPrincipal User principal, 
			@RequestParam(value = "room", required = false)String room) throws Exception{
		   String uploader = principal.getUsername();
		   String targetRoom = (room == null || room.isBlank()) ? uploader : room;
		   String key = s3Service.uploadFile(file);
		   String url = s3Service.getFileUrl(key);
		   
		   EventMessage em = new EventMessage();
		   em.setRoom(targetRoom);
		   em.setContent(url);
		   em.setSender(uploader);
		   em.setType("file");
		   em.setTimestamp(System.currentTimeMillis());
		   
		   kafkaProducerService.sendEvent(em);
		   
		   return ResponseEntity.ok(Map.of("url", url, "key", key));
		   
	}
	
	
}
