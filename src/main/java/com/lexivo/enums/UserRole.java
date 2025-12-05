package com.lexivo.enums;

public enum UserRole {
	PUBLIC,
	USER,
	ADMIN;

	public static UserRole fromString(String s) {
		return "ADMIN".equals(s) ? ADMIN : USER;
	}

	public boolean isMinimumAllowed(UserRole minimumAllowed) {
		return this == minimumAllowed || this == ADMIN;
	}
}
