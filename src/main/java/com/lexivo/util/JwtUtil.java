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
	public static final String CLAIM_EMAIL = "email";
	public static final String CLAIM_ROLE = "role";
	private static final String KEY_JWT_SECRET = "JWT_SECRET";
	public static String createHMAC256Token(String email, UserRole role, long validMinutes) {
		return JWT.create()
				.withClaim(CLAIM_EMAIL, email)
				.withClaim(CLAIM_ROLE, role.toString())
				.withExpiresAt(Instant.now().plusSeconds(DateAndTime.getSecondsInMinutes(validMinutes)))
				.sign(Algorithm.HMAC256(System.getenv(KEY_JWT_SECRET)));
	}

	public static String createAccessToken(String email, UserRole role) {
		return JwtUtil.createHMAC256Token(email, UserRole.USER, 5);
	}

	public static String createRefreshToken(String email, UserRole role) {
		return JwtUtil.createHMAC256Token(email, role, DateAndTime.getMinutesInDays(7));
	}

	public static DecodedJWT verifyJwtToken(String jwtToken) {
		try {
			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(System.getenv(KEY_JWT_SECRET)))
					.withClaimPresence(CLAIM_EMAIL)
					.withClaimPresence(CLAIM_ROLE)
					.build();
			return verifier.verify(jwtToken);
		}
		catch (JWTVerificationException ignored) {
			return null;
		}
	}

	public static boolean isMinimumAllowedRole(DecodedJWT decoded, UserRole minimumAllowedRole) {
		String roleString = decoded.getClaim(CLAIM_ROLE).asString();
		if (roleString == null) return false;
		return UserRole.fromString(roleString).isMinimumAllowed(minimumAllowedRole);
	}
}



