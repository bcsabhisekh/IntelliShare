package com.abhisekhsite.Authentication.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abhisekhsite.Authentication.model.FileRecord;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
	
}

