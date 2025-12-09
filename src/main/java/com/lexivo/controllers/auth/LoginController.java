package com.lexivo.controllers.auth;

import com.lexivo.controllers.Controller;
import com.lexivo.db.Db;
import com.lexivo.enums.UserRole;
import com.lexivo.schema.AuthReqBody;
import com.lexivo.schema.Log;
import com.lexivo.schema.User;
import com.lexivo.util.*;
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
		AuthReqBody requestBody = RequestData.getCheckedRequestBody(exchange, List.of("email", "password"), AuthReqBody.class);

		if (requestBody == null) return;

		String email = requestBody.getEmail();
		email = email == null ? null : email.trim();
		String password = requestBody.getPassword();

		User user = userLoginCheckSuccessful(exchange, email, password);

		if (user == null) return;

		if (role == UserRole.ADMIN && !adminLoginCheckSuccessful(exchange, email, requestBody.getAdminPassword())) {
			Log.warning(email, List.of("Admin login attempt"));
			return;
		}

		Map<String, String> jsonMap = new HashMap<>();
		long accessTokenValidMinutes = role == UserRole.ADMIN ? 15 : 5;
		jsonMap.put(JwtUtil.KEY_ACCESS_TOKEN, JwtUtil.createHMAC256Token(email, role, accessTokenValidMinutes));
		if (role == UserRole.USER) {
			jsonMap.put(JwtUtil.KEY_REFRESH_TOKEN, JwtUtil.createHMAC256Token(email, UserRole.USER, DateAndTime.getMinutesInDays(30)));
		}
		jsonMap.put("email", user.getEmail());
		jsonMap.put("name", user.getName());

		sendJsonResponse(exchange, HttpResponseStatus.OK, JsonUtil.toJson(jsonMap));
	}

	public static User userLoginCheckSuccessful(HttpExchange exchange, String email, String password) throws IOException {
		User user = Db.users().getByEmail(email);
		if (user == null) {
			sendIncorrectCredentialsResponse(exchange);
			return null;
		}

		boolean passwordCorrect = false;
		try {
			passwordCorrect = BCrypt.checkpw(password, user.getPasswordHash());
		} catch (RuntimeException ignore) {}

		if (!passwordCorrect) {
			sendIncorrectCredentialsResponse(exchange);
			return null;
		}

		if (!user.isConfirmed()) {
			sendConfirmationCodeEmailAndResponse(exchange, user, HttpResponseStatus.FORBIDDEN);
			return null;
		}

		return user;
	}

	private boolean adminLoginCheckSuccessful(HttpExchange exchange, String email, String adminPassword) throws IOException {
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
		Log.warning(email, List.of("Admin login"));
		return true;
	}

	private static void sendIncorrectCredentialsResponse(HttpExchange exchange) throws IOException {
		sendBadRequestResponse(exchange, "Incorrect credentials");
	}
}
