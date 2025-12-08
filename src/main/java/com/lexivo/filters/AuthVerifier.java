package com.lexivo.filters;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lexivo.controllers.Controller;
import com.lexivo.enums.UserRole;
import com.lexivo.schema.Log;
import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.JwtUtil;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class AuthVerifier extends Filter {
	private final UserRole minimumRole;

	public AuthVerifier(UserRole minimumRole) {
		this.minimumRole = minimumRole;
	}

	@Override
	public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
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
		if (!JwtUtil.isMinimumAllowedRole(decoded, minimumRole)) {
			denyAccessResponse(exchange, HttpResponseStatus.FORBIDDEN);
			return;
		}
		exchange.setAttribute(JwtUtil.CLAIM_EMAIL, decoded.getClaim(JwtUtil.CLAIM_EMAIL));
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
			Log.exception("Server side IOException in filters.AuthVerifier.denyAccessResponse", ioe.getMessage());
		}
	}
}
