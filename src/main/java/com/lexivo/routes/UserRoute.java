package com.lexivo.routes;

import com.lexivo.controllers.user.UserController;
import com.lexivo.enums.UserRole;
import com.lexivo.filters.AuthVerifier;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpServer;

public class UserRoute implements RouteWithServer {
	private final String confirmEmailPath;
	private final String recoverPasswordPath;
	private final String changePasswordPath;
	private final String changeUserNamePath;

	public UserRoute(String basePath) {
		basePath = basePath + "/user";
		this.confirmEmailPath = basePath + "/confirm_email";
		this.recoverPasswordPath = basePath + "/recover_password";
		this.changePasswordPath = basePath + "/change_password";
		this.changeUserNamePath = basePath + "/change_name";
	}

	@Override
	public void withServer(HttpServer server) {
		new Route(server, confirmEmailPath, UserController.confirmEmail(confirmEmailPath).getClass());
		new Route(server, recoverPasswordPath, UserController.recoverPassword(recoverPasswordPath).getClass());
		new Route(server, changePasswordPath, UserController.changePassword(changePasswordPath).getClass(), new Filter[]{new AuthVerifier(UserRole.USER)});
		new Route(server, changeUserNamePath, UserController.changeName(changeUserNamePath).getClass(), new Filter[]{new AuthVerifier(UserRole.USER)});
	}
}
