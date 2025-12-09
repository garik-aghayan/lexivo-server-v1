package com.lexivo.filters;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryParamsToMap extends Filter {
	public static final String ATTR_QUERY_PARAMS = "queryParams";
	@Override
	public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
		String queryString = exchange.getRequestURI().getQuery();
		Map<String, Object> map = new HashMap<>();
		if (queryString != null && !queryString.isBlank()) {
			String[] pairsString = queryString.split("&");
			List<String[]> pairs = Arrays.stream(pairsString).takeWhile(s -> s.contains("=")).map(pair -> pair.split("=")).toList();
			for (String[] pair : pairs) {
				String key = pair[0];
				Object value = pair[1];
				map.put(key, value);
			}
		};
		exchange.setAttribute(ATTR_QUERY_PARAMS, map);
		chain.doFilter(exchange);
	}

	@Override
	public String description() {
		return "Converts query params string into Map<String, Object> and sets as an attribute";
	}
}
