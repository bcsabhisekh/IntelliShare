package com.abhisekhsite.Authentication.service;

import org.springframework.stereotype.Service;

/**
 * Service for AI-based importance scoring of messages.
 * This is a placeholder implementation. In production, you would integrate
 * with an actual AI service (OpenAI, Anthropic, etc.) to score message importance.
 */
@Service
public class AIService {
	
	/**
	 * Scores the importance of a message based on its content.
	 * Returns a score between 0.0 and 1.0, where 1.0 is most important.
	 * 
	 * @param content The message content to score
	 * @param sender The sender of the message
	 * @return Importance score between 0.0 and 1.0
	 */
	public double scoreMessageImportance(String content, String sender) {
		if (content == null || content.trim().isEmpty()) {
			return 0.1;
		}
		
		// Placeholder implementation - replace with actual AI service call
		// For now, using simple heuristics:
		// - Longer messages might be more important
		// - Messages with question marks might be important
		// - Messages with keywords like "important", "urgent", etc.
		
		double score = 0.5; // Base score
		
		String lowerContent = content.toLowerCase();
		
		// Increase score for longer messages (up to a point)
		int length = content.length();
		if (length > 100) score += 0.1;
		if (length > 500) score += 0.1;
		
		// Increase score for questions
		if (content.contains("?") || lowerContent.contains("how") || 
		    lowerContent.contains("what") || lowerContent.contains("why")) {
			score += 0.15;
		}
		
		// Increase score for important keywords
		String[] importantKeywords = {"important", "urgent", "critical", "deadline", 
		                              "meeting", "decision", "action", "required"};
		for (String keyword : importantKeywords) {
			if (lowerContent.contains(keyword)) {
				score += 0.1;
				break;
			}
		}
		
		// Decrease score for very short messages
		if (length < 20) {
			score -= 0.2;
		}
		
		// Ensure score is between 0.0 and 1.0
		return Math.max(0.0, Math.min(1.0, score));
	}
	
	/**
	 * Example method signature for integrating with an actual AI service:
	 * 
	 * public double scoreMessageImportance(String content, String sender) {
	 *     // Call OpenAI API or similar
	 *     // Example prompt: "Rate the importance of this message from 0-1: {content}"
	 *     // Parse response and return score
	 * }
	 */
}
