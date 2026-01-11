package com.lexivo.handlers.lang;

import com.lexivo.db.Db;
import com.lexivo.logger.Logger;
import com.lexivo.util.HttpResponseStatus;
import org.jandle.api.annotations.HttpRequestHandler;
import org.jandle.api.http.Handler;
import org.jandle.api.http.Request;
import org.jandle.api.http.RequestMethod;
import org.jandle.api.http.Response;

import java.io.IOException;

@HttpRequestHandler(method = RequestMethod.GET, path = "/lang")
public class GetAllLangHandler implements Handler {
	private final Logger logger = new Logger();

	@Override
	public void handle(Request request, Response response) throws IOException {
		try {
			var langList = Db.lang().getAll();
			response.status(HttpResponseStatus.OK).sendJson(langList);
		}
		catch (Exception e) {
			logger.exception(e, new String[]{e.getMessage()});
			response.sendStatus(HttpResponseStatus.SERVER_SIDE_ERROR);
		}
	}
}
