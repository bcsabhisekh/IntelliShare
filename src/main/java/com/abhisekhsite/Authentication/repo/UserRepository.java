package com.abhisekhsite.Authentication.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abhisekhsite.Authentication.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
	
	Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
