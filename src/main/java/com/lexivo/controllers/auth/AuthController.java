package com.lexivo.controllers.auth;

import com.lexivo.controllers.Controller;
import com.lexivo.enums.UserRole;

public abstract class AuthController {
	public static Controller login(String path, UserRole role) {
		return new LoginController(path, role);
	}

	public static Controller signup(String path) {
		return new SignupController(path);
	}
}
