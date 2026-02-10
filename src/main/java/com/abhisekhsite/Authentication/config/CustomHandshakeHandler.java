package com.abhisekhsite.Authentication.config;

import java.security.Principal;
import java.util.Map;

import org.hibernate.query.sqm.mutation.internal.cte.CteDeleteHandler;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CustomHandshakeHandler extends DefaultHandshakeHandler{

	
	@Override
	protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, 
			Map<String, Object> attributes) {
        Object  email = attributes.get("userEmail");
        if(email != null) {
        	return new StompPrincipal(email.toString());
        }
        return super.determineUser(request, wsHandler, attributes);
	}
	
	
}

class StompPrincipal implements Principal{
	
	private final String name;
	public StompPrincipal(String name) {
		this.name = name;
		// TODO Auto-generated constructor stub
	}
	@Override
	public String getName() {
		return name;
	}
}
