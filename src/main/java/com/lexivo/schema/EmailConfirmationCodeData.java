package com.lexivo.schema;

public class EmailConfirmationCodeData {
	private final String email;
	private final String code;
	private final long createdAt;
	private final long expiresAt;

	public EmailConfirmationCodeData(String email, String code, long createdAt, long expiresAt) {
		this.email = email;
		this.code = code;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
	}

	public EmailConfirmationCodeData(String email, String code) {
		this.email = email;
		this.code = code;
		this.createdAt = System.currentTimeMillis();
		this.expiresAt = this.createdAt + 10 * 60 * 1000;
	}

	public String getEmail() {
		return email;
	}

	public String getCode() {
		return code;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public long getExpiresAt() {
		return expiresAt;
	}
}
