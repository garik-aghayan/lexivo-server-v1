package com.lexivo.filters;

import com.lexivo.controllers.Controller;
import com.lexivo.util.HttpResponseStatus;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RateLimiter extends Filter {
	private static final ConcurrentMap<String, Deque<Long>> ipMap = new ConcurrentHashMap<>();
	private final int MAX_REQUESTS_PER_MINUTE = 3;
	private static final long WINDOW_MS = 60_000;

	@Override
	public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
		String ip = exchange.getRemoteAddress().getHostString();
		long now = System.currentTimeMillis();
		Deque<Long> timestamps = ipMap.computeIfAbsent(ip, k -> new ArrayDeque<>());

		synchronized (timestamps) {
			while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
				timestamps.pollFirst();
			}

			if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
				Controller.sendResponseWithMessage(
						exchange,
						HttpResponseStatus.TOO_MANY_REQUESTS,
						"Too many requests. Try again later"
				);
				return;
			}

			timestamps.addLast(now);
		}

		chain.doFilter(exchange);
	}

	@Override
	public String description() {
		return "Rate limit per IP: " + MAX_REQUESTS_PER_MINUTE + " requests/minute";
	}
}
