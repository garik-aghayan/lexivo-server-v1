package com.lexivo.filters;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lexivo.enums.UserRole;
import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.JwtUtil;
import org.jandle.api.http.Chain;
import org.jandle.api.http.Filter;
import org.jandle.api.http.Request;
import org.jandle.api.http.Response;

import java.io.IOException;
import java.util.List;

public class AuthVerifierFilter implements Filter {
	private final UserRole minRole;

	public AuthVerifierFilter(UserRole minRole) {
		this.minRole = minRole;
	}

	public AuthVerifierFilter() {
		this.minRole = UserRole.USER;
	}

	@Override
	public void doFilter(Request request, Response response, Chain filterChain) throws IOException {
		List<String> authorization = request.getHeader("Authorization");
		if (authorization == null || authorization.size() != 1) {
			response.sendStatus(HttpResponseStatus.UNAUTHORIZED);
			return;
		}

		String token = authorization.getFirst().split(" ")[1];
		DecodedJWT decoded = JwtUtil.verifyJwtToken(token);

		if (decoded == null) {
			response.sendStatus(HttpResponseStatus.UNAUTHORIZED);
			return;
		}
		if (!JwtUtil.isMinimumAllowedRole(decoded, minRole)) {
			response.sendStatus(HttpResponseStatus.UNAUTHORIZED);
			return;
		}

		request.setAttribute(JwtUtil.CLAIM_EMAIL, decoded.getClaim(JwtUtil.CLAIM_EMAIL));

		filterChain.next(request, response);
	}
}
