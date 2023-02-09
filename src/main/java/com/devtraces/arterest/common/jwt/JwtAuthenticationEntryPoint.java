package com.devtraces.arterest.common.jwt;

import com.devtraces.arterest.common.exception.ErrorCode;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException {
		String exception = (String)request.getAttribute("exception");

		if (exception == null) {
			setResponse(response, ErrorCode.ACCESS_DENIED);
		} else if (exception.equals(ErrorCode.EXPIRED_ACCESS_TOKEN.getCode())) {
			setResponse(response, ErrorCode.EXPIRED_ACCESS_TOKEN);
		} else if (exception.equals(ErrorCode.INVALID_TOKEN.getCode())) {
			setResponse(response, ErrorCode.INVALID_TOKEN);
		} else {
			setResponse(response, ErrorCode.ACCESS_DENIED);
		}
	}

	private void setResponse(HttpServletResponse response, ErrorCode errorCode)
		throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		JSONObject responseJson = new JSONObject();
		responseJson.put("errorCode", errorCode.getCode());
		responseJson.put("errorMessage", errorCode.getMessage());

		response.getWriter().print(responseJson);
	}
}
