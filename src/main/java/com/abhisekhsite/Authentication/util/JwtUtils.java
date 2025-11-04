package com.abhisekhsite.Authentication.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtils {

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.jwt.expiration-ms}")
	private long jwtExpirationMs;
	
	public String generateToken(String email, String name) {
		return Jwts.builder()
				.setSubject(email)
				.claim("name", name)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
				.signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
				.compact();
	}
	
	public String getEmailFromJwt(String token) {
		   Claims claims = Jwts.parser()
				   .setSigningKey(jwtSecret.getBytes())
				   .parseClaimsJws(token)
				   .getBody();
		   return claims.getSubject();
	}
	
	public boolean validateJwtToken(String token) {
		try {
		    Jwts.parser().setSigningKey(jwtSecret.getBytes()).parseClaimsJws(token);
		    return true;
		}
		catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
	
}
