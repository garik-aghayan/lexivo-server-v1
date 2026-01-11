package com.lexivo.handlers.dict;

import com.lexivo.filters.AuthVerifierFilter;
import com.lexivo.util.HttpResponseStatus;
import org.jandle.api.annotations.HttpRequestFilters;
import org.jandle.api.annotations.HttpRequestHandler;
import org.jandle.api.http.Handler;
import org.jandle.api.http.Request;
import org.jandle.api.http.RequestMethod;
import org.jandle.api.http.Response;

import java.io.IOException;

@HttpRequestHandler(method = RequestMethod.GET, path = "/dict")
@HttpRequestFilters({ AuthVerifierFilter.class })
public class GetAllDictHandler implements Handler {
	@Override
	public void handle(Request request, Response response) throws IOException {
		//		TODO: Implement
		response.sendStatus(HttpResponseStatus.OK);
	}
}
