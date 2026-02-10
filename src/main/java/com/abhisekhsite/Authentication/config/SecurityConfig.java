package com.abhisekhsite.Authentication.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.abhisekhsite.Authentication.service.CustomUserDetailsService;
import com.abhisekhsite.Authentication.util.JwtUtils;

@Configuration
public class SecurityConfig {

	  @Autowired
	  private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
	
	  @Autowired
	  private JwtUtils jwtUtils;
	  
	  @Autowired
	  private CustomUserDetailsService customUserDetailsService;
	  
	  @Bean
	  public BCryptPasswordEncoder passwordEncoder() {
		  return new BCryptPasswordEncoder();
	  }
	  
	  @Bean
	  SecurityFilterChain securityFilterChain(HttpSecurity http) throws
	  Exception {
		  JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtils, customUserDetailsService);
		  http.
		  csrf(csrf -> csrf.disable())
		  .authorizeHttpRequests(auth -> auth.requestMatchers("/", "/auth/**", "/oauth2/**"
				  , "/h2-console/**").permitAll()
				  .anyRequest().authenticated()
		  )
          .exceptionHandling(ex -> ex.authenticationEntryPoint(
                  (request, response, authException) -> response.sendError(401, "Unauthorized")
              ))
		  .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2LoginSuccessHandler))
		  .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		  
		  http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
		  
		  return http.build();
	  }
	  
	  
}
