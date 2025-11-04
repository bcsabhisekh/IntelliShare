package com.abhisekhsite.Authentication.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.abhisekhsite.Authentication.model.User;
import com.abhisekhsite.Authentication.repo.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService{
	
	private final UserRepository userRepository;
	
	public CustomUserDetailsService(UserRepository repo) {
		this.userRepository = repo;
	}
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

	    return org.springframework.security.core.userdetails.User
	            .withUsername(user.getEmail())
	            .password(user.getPassword() == null ? "" : user.getPassword())
	            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
	            .accountExpired(false)
	            .accountLocked(false)
	            .credentialsExpired(false)
	            .disabled(false)
	            .build();
	}

}
