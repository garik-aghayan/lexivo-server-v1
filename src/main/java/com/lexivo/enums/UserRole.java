package com.lexivo.enums;

public enum UserRole {
	PUBLIC,
	USER,
	ADMIN;

	public static UserRole fromString(String s) {
		return switch (s) {
			case "USER" -> USER;
			case "ADMIN" -> ADMIN;
			default -> PUBLIC;
		};
	}

	public boolean isMinimumAllowed(UserRole minimumAllowed) {
		return minimumAllowed == PUBLIC || this == minimumAllowed || this == ADMIN;
	}
}
