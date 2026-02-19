package com.abhisekhsite.Authentication.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abhisekhsite.Authentication.model.Room;
import com.abhisekhsite.Authentication.model.RoomMessage;
import com.abhisekhsite.Authentication.repo.RoomMessageRepository;
import com.abhisekhsite.Authentication.repo.RoomRepository;
import com.abhisekhsite.Authentication.service.RedisService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
	
	private final RoomRepository roomRepository;
	private final RoomMessageRepository messageRepository;
	private final RedisService redisService;
	
	public RoomController(RoomRepository roomRepository, 
	                      RoomMessageRepository messageRepository,
	                      RedisService redisService) {
		this.roomRepository = roomRepository;
		this.messageRepository = messageRepository;
		this.redisService = redisService;
	}
	
	public static record CreateRoomRequest(
		@NotBlank 
		@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Room ID must contain only letters, numbers, underscores, or hyphens")
		String roomId,
		String name
	) {}
	
	/**
	 * Create a new room
	 */
	@PostMapping("/create")
	public ResponseEntity<?> createRoom(@Valid @RequestBody CreateRoomRequest req,
	                                    @AuthenticationPrincipal User principal) {
		String email = principal.getUsername();
		
		if (roomRepository.existsByRoomId(req.roomId())) {
			return ResponseEntity.badRequest()
				.body(Map.of("error", "Room ID already exists"));
		}
		
		Room room = new Room(req.roomId(), req.name(), email);
		room = roomRepository.save(room);
		
		Map<String, Object> response = new HashMap<>();
		response.put("roomId", room.getRoomId());
		response.put("name", room.getName());
		response.put("createdBy", room.getCreatedBy());
		response.put("createdAt", room.getCreatedAt());
		
		return ResponseEntity.ok(response);
	}
	
	/**
	 * Get room details
	 */
	@GetMapping("/info")
	public ResponseEntity<?> getRoomInfo(@RequestParam String roomId) {
		return roomRepository.findByRoomId(roomId)
			.map(room -> {
				Map<String, Object> response = new HashMap<>();
				response.put("roomId", room.getRoomId());
				response.put("name", room.getName());
				response.put("createdBy", room.getCreatedBy());
				response.put("createdAt", room.getCreatedAt());
				return ResponseEntity.ok(response);
			})
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
	
	/**
	 * Get messages for a room
	 * First checks Redis cache, then fetches top 10 important messages from DB
	 */
	@GetMapping("/messages")
	public ResponseEntity<?> getMessages(@RequestParam String roomId) {
		// Verify room exists
		if (!roomRepository.existsByRoomId(roomId)) {
			return ResponseEntity.badRequest()
				.body(Map.of("error", "Room not found"));
		}
		
		// Try Redis first
		List<RoomMessage> messages = redisService.getRecentMessages(roomId);
		
		if (messages != null && !messages.isEmpty()) {
			return ResponseEntity.ok(Map.of("messages", messages, "source", "cache"));
		}
		
		// If Redis is empty, fetch top 10 important messages from DB
		messages = messageRepository.findTop10ImportantMessagesByRoom(roomId);
		
		// If no important messages, get recent messages
		if (messages.isEmpty()) {
			messages = messageRepository.findByRoomOrderByCreatedAtDesc(roomId);
			if (messages.size() > 10) {
				messages = messages.subList(0, 10);
			}
		}
		
		// Cache the results
		if (!messages.isEmpty()) {
			redisService.cacheRecentMessages(roomId, messages);
		}
		
		return ResponseEntity.ok(Map.of("messages", messages, "source", "database"));
	}
	
	/**
	 * Get all rooms created by the current user
	 */
	@GetMapping("/my-rooms")
	public ResponseEntity<?> getMyRooms(@AuthenticationPrincipal User principal) {
		String email = principal.getUsername();
		List<Room> rooms = roomRepository.findAll().stream()
			.filter(room -> email.equals(room.getCreatedBy()))
			.collect(Collectors.toList());
		
		return ResponseEntity.ok(Map.of("rooms", rooms));
	}
}
