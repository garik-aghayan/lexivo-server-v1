package com.lexivo.handlers.logs;

import com.lexivo.db.Db;
import com.lexivo.exceptions.InvalidLogCategoryException;
import com.lexivo.logger.Logger;
import com.lexivo.schema.Log;
import com.lexivo.util.HttpResponseStatus;
import org.jandle.api.annotations.HttpRequestHandler;
import org.jandle.api.http.Handler;
import org.jandle.api.http.Request;
import org.jandle.api.http.RequestMethod;
import org.jandle.api.http.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@HttpRequestHandler(method = RequestMethod.GET, path = "/logs")
public class GetAllLogsHandler implements Handler {
	private final Logger logger = new Logger();
	@Override
	public void handle(Request request, Response response) throws IOException {
		Log.Category[] categories = getCategories(request.getQueryParamFirst("categories"));
		String userEmail = request.getQueryParamFirst("email");
		long[] dates = getDates(request.getQueryParamFirst("dateFrom"), request.getQueryParamFirst("dateTo"));
		long dateFrom = dates[0];
		long dateTo = dates[1];


		List<Log> logs = Db.logs().getLogs(categories, userEmail, dateFrom, dateTo);
		try {
			response
					.status(HttpResponseStatus.OK)
					.sendJson(logs);
		}
		catch (Exception e) {
			logger.exception(e, userEmail, new String[]{e.getMessage()});
			response.sendStatus(HttpResponseStatus.SERVER_SIDE_ERROR);
		}
	}

	private Log.Category[] getCategories(String categoriesObject) {
		if (categoriesObject == null) return null;
		List<Log.Category> categories = new ArrayList<>();
		Arrays.stream((categoriesObject).split(",")).forEach(categoryString -> {
			try {
				Log.Category category = Log.Category.fromString(categoryString.trim().toUpperCase());
				categories.add(category);
			} catch (InvalidLogCategoryException ignored) {}
		});
		return categories.toArray(new Log.Category[0]);
	}

	private long[] getDates(Object dateFrom, Object dateTo) {
		long df;
		long dt;
		try {
			df = Long.parseLong((String) dateFrom);

		} catch (NumberFormatException nfe) {
			df = 0;
		}
		try {
			dt = Long.parseLong((String) dateTo);
		}
		catch (NumberFormatException nfe) {
			dt = System.currentTimeMillis();
		}
		return new long[]{df, dt};
	}
}
