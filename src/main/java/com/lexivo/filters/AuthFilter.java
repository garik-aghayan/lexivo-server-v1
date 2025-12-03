package com.lexivo.filters;

import com.lexivo.enums.UserRole;
import com.lexivo.util.HttpResponseStatus;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
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
			// TODO: Check token validity
			// TODO: Check ROLE
		}
		chain.doFilter(exchange);
	}

	@Override
	public String description() {
		return "Checks if the user has authorization to access the endpoint";
	}

	private void denyAccessResponse(HttpExchange exchange, int responseCode) {
		try {
			exchange.sendResponseHeaders(responseCode, 0);
			OutputStream os = exchange.getResponseBody();
			os.write(new byte[]{});
			os.close();
		}
		catch (IOException ioe) {
			// TODO: Replace with a proper logger
			System.err.println(ioe.getMessage());
		}
	}

}
