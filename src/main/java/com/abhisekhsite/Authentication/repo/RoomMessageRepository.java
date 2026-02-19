package com.abhisekhsite.Authentication.repo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.abhisekhsite.Authentication.model.RoomMessage;

public interface RoomMessageRepository extends JpaRepository<RoomMessage, Long> {
	
	List<RoomMessage> findByRoomOrderByCreatedAtDesc(String room);
	
	@Query("SELECT rm FROM RoomMessage rm WHERE rm.room = :room AND rm.importanceScore IS NOT NULL ORDER BY rm.importanceScore DESC, rm.createdAt DESC")
	List<RoomMessage> findTop10ByRoomOrderByImportanceScoreDesc(String room, Pageable pageable);
	
	default List<RoomMessage> findTop10ImportantMessagesByRoom(String room) {
		return findTop10ByRoomOrderByImportanceScoreDesc(room, Pageable.ofSize(10));
	}
}

