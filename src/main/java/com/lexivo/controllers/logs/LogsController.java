package com.lexivo.controllers.logs;

import com.lexivo.controllers.Controller;
import com.lexivo.db.Db;
import com.lexivo.exceptions.InvalidLogCategoryException;
import com.lexivo.filters.QueryParamsToMap;
import com.lexivo.schema.Log;
import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LogsController extends Controller {
	public LogsController(String path) {
		super(path);
	}

	@Override
	protected void get(HttpExchange exchange) throws IOException {
		@SuppressWarnings("unchecked")
		Map<String, Object> queryParams = (Map<String, Object>) exchange.getAttribute(QueryParamsToMap.ATTR_QUERY_PARAMS);
		Log.Category[] categories = getCategories(queryParams.get("categories"));
		String userEmail =(String)queryParams.get("email");
		long[] dates = getDates(queryParams.get("dateFrom"), queryParams.get("dateTo"));
		long dateFrom = dates[0];
		long dateTo = dates[1];

		List<Log> logs = Db.logs().getLogs(categories, userEmail, dateFrom, dateTo);

		sendJsonResponse(exchange, HttpResponseStatus.OK, JsonUtil.toJson(Map.of("logs", logs)));
	}

	private Log.Category[] getCategories(Object categoriesObject) {
		if (categoriesObject == null) return null;
		List<Log.Category> categories = new ArrayList<>();
		Arrays.stream(((String) categoriesObject).split(",")).forEach(categoryString -> {
			try {
				Log.Category category = Log.Category.fromString(categoryString.trim());
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
