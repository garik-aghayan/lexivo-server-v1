package com.lexivo.filters;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lexivo.controllers.Controller;
import com.lexivo.enums.UserRole;
import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.JwtUtil;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class AuthFilter extends Filter {
	private final UserRole minimumRole;

	public AuthFilter(UserRole minimumRole) {
		this.minimumRole = minimumRole;
	}

	@Override
	public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
		if (minimumRole != UserRole.PUBLIC) {
			Headers headers = exchange.getRequestHeaders();
			List<String> authorization = headers.get("Authorization");
			if (authorization == null || authorization.size() != 1) {
				denyAccessResponse(exchange, HttpResponseStatus.UNAUTHORIZED);
				return;
			}
			String token = authorization.getFirst().split(" ")[1];
			DecodedJWT decoded = JwtUtil.verifyJwtToken(token);
			if (decoded == null) {
				denyAccessResponse(exchange, HttpResponseStatus.UNAUTHORIZED);
				return;
			}
			if (JwtUtil.isMinimumAllowedRole(decoded, minimumRole)) {
				denyAccessResponse(exchange, HttpResponseStatus.FORBIDDEN);
				return;
			}
		}
		chain.doFilter(exchange);
	}

	@Override
	public String description() {
		return "Checks if the user is allowed to access the endpoint";
	}

	private void denyAccessResponse(HttpExchange exchange, int responseCode) {
		try {
			Controller.sendJsonResponse(exchange, responseCode, "");
		}
		catch (IOException ioe) {
			// TODO: Replace with a proper logger
			System.err.println(ioe.getMessage());
		}
	}
}
