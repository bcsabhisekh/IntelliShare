package com.abhisekhsite.Authentication.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.abhisekhsite.Authentication.model.Provider;
import com.abhisekhsite.Authentication.model.User;
import com.abhisekhsite.Authentication.repo.UserRepository;
import com.abhisekhsite.Authentication.util.JwtUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler{
	
	private final JwtUtils jwtUtils;
	private final UserRepository userRepository;
	
	@Value("${app.frontend.url}")
	private String frontendUrl;
	
	public OAuth2LoginSuccessHandler(JwtUtils jwtUtils, UserRepository userRepository) {
		this.jwtUtils = jwtUtils;
		this.userRepository = userRepository;
	}
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException{
		
		Object principal = authentication.getPrincipal();
		if(principal instanceof OidcUser oidcUser) {
			String email = oidcUser.getAttribute("email");
			String name = oidcUser.getAttribute("name");
			String picture = oidcUser.getAttribute("picture");
			
			Optional<User> opt = userRepository.findByEmail(email);
			
			if(opt.isEmpty()) {
				User user = new User(email, name, null, Provider.GOOGLE, picture);
				userRepository.save(user);
			} else {
				User user = opt.get();
				user.setName(name);
				user.setPicture(picture);
				userRepository.save(user);
			}
			
			String token = jwtUtils.generateToken(email, name);
			String redirectUrl = frontendUrl + "/?token=" + token;
			response.sendRedirect(redirectUrl);
			
		} else {
			response.sendRedirect(frontendUrl + "/?error");
		}
	}

}
