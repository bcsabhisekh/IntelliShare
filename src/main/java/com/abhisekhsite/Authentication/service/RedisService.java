package com.abhisekhsite.Authentication.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.abhisekhsite.Authentication.model.RoomMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RedisService {
	
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;
	private static final String RECENT_MESSAGES_KEY = "recent:messages:";
	private static final String RECENT_FILES_KEY = "recent:files:";
	private static final long CACHE_TTL_HOURS = 24; // Cache for 24 hours
	
	public RedisService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = new ObjectMapper();
	}
	
	/**
	 * Cache recent messages for a room
	 */
	public void cacheRecentMessages(String room, List<RoomMessage> messages) {
		try {
			String key = RECENT_MESSAGES_KEY + room;
			String json = objectMapper.writeValueAsString(messages);
			redisTemplate.opsForValue().set(key, json, CACHE_TTL_HOURS, TimeUnit.HOURS);
		} catch (JsonProcessingException e) {
			System.err.println("Error caching messages to Redis: " + e.getMessage());
		}
	}
	
	/**
	 * Get recent messages from cache
	 */
	public List<RoomMessage> getRecentMessages(String room) {
		try {
			String key = RECENT_MESSAGES_KEY + room;
			String json = redisTemplate.opsForValue().get(key);
			if (json != null) {
				return objectMapper.readValue(json, new TypeReference<List<RoomMessage>>() {});
			}
		} catch (JsonProcessingException e) {
			System.err.println("Error reading messages from Redis: " + e.getMessage());
		}
		return null;
	}
	
	/**
	 * Add a single message to the cache
	 */
	public void addMessageToCache(String room, RoomMessage message) {
		List<RoomMessage> cached = getRecentMessages(room);
		if (cached != null) {
			cached.add(0, message); // Add to beginning
			// Keep only last 100 messages in cache
			if (cached.size() > 100) {
				cached = cached.subList(0, 100);
			}
			cacheRecentMessages(room, cached);
		} else {
			// Initialize cache with this message
			cacheRecentMessages(room, List.of(message));
		}
	}
	
	/**
	 * Clear cache for a room
	 */
	public void clearRoomCache(String room) {
		redisTemplate.delete(RECENT_MESSAGES_KEY + room);
		redisTemplate.delete(RECENT_FILES_KEY + room);
	}
}
