package com.lexivo.handlers.user;

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

@HttpRequestHandler(method = RequestMethod.PUT, path = "/user/change_password")
public class ChangePasswordHandler implements Handler {
	private final Logger logger = new Logger();

	@Override
	public void handle(Request request, Response response) throws IOException {
		var requestBody = request.getBodyJson();

		String email = (String) request.getAttribute(JwtUtil.CLAIM_EMAIL);
		String password = (String) requestBody.get("password");
		String newPassword = (String) requestBody.get("newPassword");

		try {
			String[] missingData = ValidationUtil.getMissingStrings(new String[]{email, password, newPassword}, List.of("Email is missing", "Password is missing", "New password is missing"));
			if (missingData.length > 0) {
				StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, missingData);
				return;
			}

			User user = Db.users().getByEmail(email);

			if (user == null) {
				StandardResponse.incorrectCredentials(response);
			};

			AuthUtil.isUserPasswordCorrect(user, password);


			if (!ValidationUtil.isPasswordValid(newPassword)) {
				StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, ValidationUtil.PASSWORD_REQUIREMENTS);
				return;
			}

			String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
			boolean success = Db.users().updateUserPassword(email, newPasswordHash);

			if (!success) {
				logger.exception(email, new String[]{"Password could not be updated"});
				response.sendStatus(HttpResponseStatus.SERVER_SIDE_ERROR);
				return;
			}

			response.sendStatus(HttpResponseStatus.OK);
		}
		catch (Exception e) {
			logger.exception(e, email, new String[]{ e.getMessage() });
			response.sendStatus(HttpResponseStatus.SERVER_SIDE_ERROR);
		}
	}
}
