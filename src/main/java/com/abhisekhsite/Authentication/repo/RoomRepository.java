package com.abhisekhsite.Authentication.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abhisekhsite.Authentication.model.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
	Optional<Room> findByRoomId(String roomId);
	boolean existsByRoomId(String roomId);
}
