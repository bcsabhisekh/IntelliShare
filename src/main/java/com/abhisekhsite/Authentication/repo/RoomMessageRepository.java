package com.abhisekhsite.Authentication.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abhisekhsite.Authentication.model.RoomMessage;

public interface RoomMessageRepository extends JpaRepository<RoomMessage, Long> {
	
}

