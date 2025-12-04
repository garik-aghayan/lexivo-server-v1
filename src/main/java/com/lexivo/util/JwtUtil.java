package com.lexivo.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lexivo.enums.UserRole;

import java.time.Instant;

public abstract class JwtUtil {
	public static final String KEY_ACCESS_TOKEN = "accessToken";
	public static final String KEY_REFRESH_TOKEN = "refreshToken";
	public static final String CLAIM_USERNAME = "username";
	public static final String CLAIM_ROLE = "role";
	private static final String KEY_JWT_SECRET = "JWT_SECRET";
	public static String createHMAC256Token(String username, UserRole role, long validMinutes) {
		return JWT.create()
				.withClaim(CLAIM_USERNAME, username)
				.withClaim(CLAIM_ROLE, role.toString())
				.withExpiresAt(Instant.now().plusSeconds(TimeUtil.getSecondsInMinutes(validMinutes)))
				.sign(Algorithm.HMAC256(System.getenv(KEY_JWT_SECRET)));
	}

	public static DecodedJWT verifyJwtToken(String jwtToken) {
		try {
			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(System.getenv(KEY_JWT_SECRET)))
					.withClaimPresence(CLAIM_USERNAME)
					.withClaimPresence(CLAIM_ROLE)
					.build();
			return verifier.verify(jwtToken);
		}
		catch (JWTVerificationException e) {
			// TODO: Replace with a proper logger
			System.out.println("Not verified token: " + jwtToken);
			return null;
		}
	}

	public static boolean isMinimumAllowedRole(DecodedJWT decoded, UserRole minimumAllowedRole) {
		String roleString = decoded.getClaim(CLAIM_ROLE).asString();
		if (roleString == null) return false;
		return UserRole.fromString(roleString).isMinimumAllowed(minimumAllowedRole);
	}
}



