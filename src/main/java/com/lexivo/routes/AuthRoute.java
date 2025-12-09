package com.lexivo.routes;

import com.lexivo.controllers.auth.AuthController;
import com.lexivo.enums.UserRole;
import com.sun.net.httpserver.HttpServer;

import java.util.List;

public class AuthRoute implements RouteWithServer {
	private final String loginPath;
	private final String adminLoginPath;
	private final String signupPath;
	private final String refreshTokenPath;

	public AuthRoute(String basePath) {
		basePath = basePath + "/auth";
		this.loginPath = basePath + "/login";
		this.adminLoginPath = basePath + "/admin_login";
		this.signupPath = basePath + "/signup";
		this.refreshTokenPath = basePath + "/refresh_token";
	}

	@Override
	public void withServer(HttpServer server) {
		new Route(server, loginPath, AuthController.login(loginPath, UserRole.USER).getClass(), List.of(UserRole.USER));
		new Route(server, adminLoginPath, AuthController.login(adminLoginPath, UserRole.ADMIN).getClass(), List.of(UserRole.ADMIN));
		new Route(server, signupPath, AuthController.signup(signupPath).getClass());
		new Route(server, refreshTokenPath, AuthController.refreshToken(refreshTokenPath).getClass());
	}
}