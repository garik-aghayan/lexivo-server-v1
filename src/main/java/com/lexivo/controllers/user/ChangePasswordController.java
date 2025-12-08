package com.lexivo.controllers.user;

import com.lexivo.controllers.Controller;
import com.lexivo.controllers.auth.LoginController;
import com.lexivo.db.Db;
import com.lexivo.schema.User;
import com.lexivo.util.JwtUtil;
import com.lexivo.util.RequestData;
import com.sun.net.httpserver.HttpExchange;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ChangePasswordController extends Controller {
	public ChangePasswordController(String path) {
		super(path);
	}

	@Override
	protected void put(HttpExchange exchange) throws IOException, SQLException {
		User requestBody = RequestData.getCheckedRequestBody(exchange, List.of("password", "newPassword"), User.class);

		if (requestBody == null) return;

		String email = RequestData.getAttributeString(exchange, JwtUtil.CLAIM_EMAIL);
		String password = requestBody.getPassword();
		String newPassword = requestBody.getNewPassword();
		User user = LoginController.userLoginCheckSuccessful(exchange, email, password);

		if (user == null) return;

		if (!RequestData.doesPasswordMeetRequirements(newPassword)) {
			sendBadRequestResponse(exchange, RequestData.PASSWORD_REQUIREMENTS);
			return;
		}

		String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
		boolean success = Db.users().updateUserPassword(email, newPasswordHash);

		if (!success) {
			sendServerSideErrorResponse(exchange);
			return;
		}

		sendOkResponse(exchange);
	}
}
