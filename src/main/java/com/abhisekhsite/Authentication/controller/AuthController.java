package com.abhisekhsite.Authentication.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abhisekhsite.Authentication.model.Provider;
import com.abhisekhsite.Authentication.model.User;
import com.abhisekhsite.Authentication.repo.UserRepository;
import com.abhisekhsite.Authentication.util.JwtUtils;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final JwtUtils jwtUtils;
	
	public AuthController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
			JwtUtils jwtUtils) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtils = jwtUtils;
	}
	
	public static record SignupRequest(@NotBlank @Email String email,
			@NotBlank @Size(min = 3) String name,
			@NotBlank @Size(min = 8) String password) {}
	
	public static record LoginRequest(@NotBlank @Email String email,
			@NotBlank String password) {}
	
	
	@PostMapping("/signup")
	public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
		if(userRepository.existsByEmail(req.email())) {
			return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
		}
		
		String encoded = passwordEncoder.encode(req.password());
		User user = new User(req.email(), req.name(), encoded, Provider.LOCAL, null);
		System.out.println(user);
		userRepository.save(user);
		System.out.println("token: " + "hii");
		String token = jwtUtils.generateToken(user.getEmail(), user.getName());
		return ResponseEntity.ok(Map.of("token", token, "name", user.getName(), "email", user.getEmail()));
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
		return userRepository.findByEmail(req.email())
				.map(user -> {
					if(user.getProvider() != Provider.LOCAL) {
						return ResponseEntity.badRequest().body(Map.of("error", "Use OAuth login for this account"));
					}
					
					if(!passwordEncoder.matches(req.password(), user.getPassword())) {
						return ResponseEntity.badRequest().body(Map.of("error", "Invalid Ceredentials"));
					}
					
					String token = jwtUtils.generateToken(user.getEmail(), user.getName());
					return ResponseEntity.ok(Map.of("token", token, "name", user.getName(), "email", user.getEmail()));
				})
				.orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Invalid credentials")));
	}
	
	@GetMapping("/me")
	public ResponseEntity<?> me(Authentication authentication){
		if(authentication == null || !authentication.isAuthenticated()) {
			System.out.println("hello");
			return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));
		}
		String email = authentication.getName();
		return userRepository.findByEmail(email)
				.map(user -> {Map<String, Object> userMap = new HashMap<>();
				userMap.put("email", user.getEmail());
				userMap.put("name", user.getName());
				userMap.put("picture", user.getPicture());
				userMap.put("provider", user.getProvider());
				return ResponseEntity.ok(userMap);}
				)
				.orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "User not found")));
	}
	
	
}
