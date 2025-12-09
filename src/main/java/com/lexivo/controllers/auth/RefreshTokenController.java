package com.lexivo.controllers.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lexivo.controllers.Controller;
import com.lexivo.enums.UserRole;
import com.lexivo.schema.AuthReqBody;
import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.JsonUtil;
import com.lexivo.util.JwtUtil;
import com.lexivo.util.RequestData;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class RefreshTokenController extends Controller {
	public RefreshTokenController(String path) {
		super(path);
	}

	@Override
	protected void post(HttpExchange exchange) throws IOException, SQLException {
		AuthReqBody requestBody = RequestData.getCheckedRequestBody(exchange, List.of(JwtUtil.KEY_REFRESH_TOKEN), AuthReqBody.class);
		if (requestBody == null) return;

		String refreshToken = requestBody.getRefreshToken();

		DecodedJWT decoded = JwtUtil.verifyJwtToken(refreshToken);
		if (decoded == null) {
			sendJsonResponse(exchange, HttpResponseStatus.UNAUTHORIZED, "");
			return;
		}

		String accessToken = JwtUtil.createHMAC256Token(decoded.getClaim(JwtUtil.CLAIM_EMAIL).asString(), UserRole.USER, 5);

		sendJsonResponse(exchange, HttpResponseStatus.OK, JsonUtil.toJson(Map.of(JwtUtil.KEY_ACCESS_TOKEN, accessToken)));
	}
}
