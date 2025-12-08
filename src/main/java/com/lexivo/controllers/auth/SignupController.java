package com.lexivo.controllers.auth;

import com.lexivo.controllers.Controller;
import com.lexivo.db.Db;
import com.lexivo.schema.Log;
import com.lexivo.schema.User;
import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.RequestData;
import com.sun.net.httpserver.HttpExchange;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class SignupController extends Controller {
	public SignupController(String path) {
		super(path);
	}

	@Override
	protected void post(HttpExchange exchange) throws IOException, SQLException {
		User requestBody = RequestData.getCheckedRequestBody(exchange, List.of("name", "email", "password"), User.class);
		if (requestBody == null) return;

		String name = requestBody.getName();
		String email = requestBody.getEmail();
		String password = requestBody.getPassword();

		User user = Db.users().getByEmail(email);
		if (user != null) {
			sendBadRequestResponse(exchange, "User with the given email already exists");
			return;
		}

		boolean passwordMeetsRequirements = RequestData.doesPasswordMeetRequirements(password);
		if (!passwordMeetsRequirements) {
			sendBadRequestResponse(exchange, RequestData.PASSWORD_REQUIREMENTS);
			return;
		}

		String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
		User newUser = new User(email, name, passwordHash, false);
		boolean success = Db.users().addUser(newUser);

		if (!success) {
			sendServerSideErrorResponse(exchange);
			return;
		}

		Log.newUser(newUser.getEmail(), List.of());

		sendConfirmationCodeEmailAndResponse(exchange, newUser, HttpResponseStatus.OK);
	}
}
