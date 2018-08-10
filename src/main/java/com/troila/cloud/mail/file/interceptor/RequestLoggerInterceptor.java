package com.troila.cloud.mail.file.interceptor;

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLoggerInterceptor implements HandlerInterceptor {
	private static final Logger LOGGER = LoggerFactory.getLogger("invocation");
	private static final String KEY_REQUEST_ID = "__request_id__";

	private AtomicLong requestId = new AtomicLong(0);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String path = request.getServletPath();
		String method = request.getMethod();
		String query = request.getQueryString();
		if (!StringUtils.isEmpty(query)) {
			path += "?" + query;
		}
		boolean hasBody = (!method.equals("GET") && request.getContentLength() > 0);
		String bodyParam = "";
		if (hasBody) {
			bodyParam = "with HTTP BODY {type:" + request.getContentType() + ", length:" + request.getContentLength()
					+ "}";
		}
		String from = request.getHeader("X-Real-IP");
		if(from == null) {
			from = request.getRemoteAddr();
		}
		long id = requestId.incrementAndGet();
		request.setAttribute(KEY_REQUEST_ID, id);
		LOGGER.info("REQUEST:{} {} {} FROM {} {}", id, method, path, from, bodyParam);

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		LOGGER.info("RESPOND TO REQUEST:{} WITH STATUS {}", request.getAttribute(KEY_REQUEST_ID), response.getStatus());
	}

}
