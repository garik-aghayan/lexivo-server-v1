package com.lexivo.handlers.auth;

import com.lexivo.db.Db;
import com.lexivo.logger.Logger;
import com.lexivo.schema.User;
import com.lexivo.util.*;
import org.jandle.api.annotations.HttpRequestHandler;
import org.jandle.api.http.Handler;
import org.jandle.api.http.Request;
import org.jandle.api.http.RequestMethod;
import org.jandle.api.http.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@HttpRequestHandler(method = RequestMethod.POST, path = "/auth/signup")
public class SignupHandler implements Handler {
	private final Logger logger = new Logger();

	@Override
	public void handle(Request request, Response response) throws IOException {
		var requestBody = request.getBodyJson();

		String name = (String) requestBody.get("name");
		String email = (String) requestBody.get("email");
		String password = (String) requestBody.get("password");


		try {
			String[] missingData = ValidationUtil.getMissingStrings(new String[]{email, password, name}, List.of("Email is missing", "Password is missing", "Name is missing"));
			if (missingData.length > 0) {
				StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, missingData);
			}

			User user = Db.users().getByEmail(email);
			if (user != null) {
				StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, "User with the given email already exists");
				return;
			}

			boolean passwordMeetsRequirements = ValidationUtil.isPasswordValid(password);
			if (!passwordMeetsRequirements) {
				StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, ValidationUtil.PASSWORD_REQUIREMENTS);
				return;
			}

			String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
			user = new User(email, name, passwordHash, false);
			boolean userAdded = Db.users().addUser(user);

			if (!userAdded) {
				response.sendStatus(HttpResponseStatus.SERVER_SIDE_ERROR);
				return;
			}

			logger.info(user.getEmail(), new String[]{});

			StandardResponse.sendConfirmationCode(response, user);
		}
		catch (Exception e) {
			logger.exception(e, email, new String[]{e.getMessage()});
			response.sendStatus(HttpResponseStatus.SERVER_SIDE_ERROR);
		}
	}
}
