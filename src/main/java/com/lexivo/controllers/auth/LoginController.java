package com.lexivo.controllers.auth;

import com.lexivo.controllers.Controller;
import com.lexivo.db.Db;
import com.lexivo.enums.UserRole;
import com.lexivo.schema.User;
import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.JsonUtil;
import com.lexivo.util.JwtUtil;
import com.lexivo.util.RequestDataCheck;
import com.sun.net.httpserver.HttpExchange;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginController extends Controller {
	private final UserRole role;

	public LoginController(String routeBasePath, UserRole role) {
		super(routeBasePath);
		this.role = role;
	}

	@Override
	protected void post(HttpExchange exchange) throws IOException {
		User requestBody = RequestDataCheck.getCheckedRequestBody(exchange, List.of("email", "password"), User.class);

		if (requestBody == null) return;

		String email = requestBody.getEmail();
		email = email == null ? null : email.trim();
		String password = requestBody.getPassword();

		if (!userLoginCheckSuccessful(exchange, email, password)) return;

		if (role == UserRole.ADMIN && !adminLoginCheckSuccessful(exchange, requestBody.getAdminPassword())) return;

		Map<String, String> jsonMap = new HashMap<>();
		jsonMap.put(JwtUtil.KEY_ACCESS_TOKEN, JwtUtil.createAccessToken(email));
		jsonMap.put(JwtUtil.KEY_REFRESH_TOKEN, JwtUtil.createRefreshToken(email));

		sendJsonResponse(exchange, HttpResponseStatus.OK, JsonUtil.toJson(jsonMap));
	}

	private boolean userLoginCheckSuccessful(HttpExchange exchange, String email, String password) throws IOException {
		User user = Db.users().getByEmail(email);
		if (user == null) {
			sendIncorrectCredentialsResponse(exchange);
			return false;
		}

		boolean passwordCorrect = false;
		try {
			passwordCorrect = BCrypt.checkpw(password, user.getPasswordHash());
		} catch (RuntimeException ignore) {}

		if (!passwordCorrect) {
			sendIncorrectCredentialsResponse(exchange);
			return false;
		}

		if (!user.isConfirmed()) {
			sendConfirmationCodeEmailAndResponse(exchange, user, HttpResponseStatus.FORBIDDEN);
			return false;
		}

		return true;
	}

	private boolean adminLoginCheckSuccessful(HttpExchange exchange, String adminPassword) throws IOException {
		if (adminPassword == null || adminPassword.isBlank()) {
			sendIncorrectCredentialsResponse(exchange);
			return false;
		}
		String adminPasswordHash = System.getenv("ADMIN_PASSWORD_HASH");
		boolean passwordCorrect = BCrypt.checkpw(adminPassword, adminPasswordHash);
		if (!passwordCorrect) {
			sendIncorrectCredentialsResponse(exchange);
			return false;
		}
		return true;
	}

	private void sendIncorrectCredentialsResponse(HttpExchange exchange) throws IOException {
		sendBadRequestResponse(exchange, "Incorrect credentials");
	}
}
