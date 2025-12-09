package com.lexivo.schema;

public class AuthReqBody {
	private String name;
	private String email;
	private String password;
	private String adminPassword;
	private String newPassword;
	private String refreshToken;

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	@Override
	public String toString() {
		return "AuthReqBody{" +
				"name='" + name + '\'' +
				", email='" + email + '\'' +
				", isPasswordNullOrBlank='" + (password == null || password.isBlank()) + '\'' +
				", isAdminPasswordIsNullOrBlank='" + (adminPassword == null || adminPassword.isBlank()) + '\'' +
				", isNewPasswordNullOrBlank='" + (newPassword == null || newPassword.isBlank()) + '\'' +
				", isRefreshTokenNullOrBlank='" + (refreshToken == null || refreshToken.isBlank()) + '\'' +
				'}';
	}
}
