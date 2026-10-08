package com.ramkrishna.sales.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
	private static final String REQUEST_ID_HEADER = "X-Request-Id";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String requestId = UUID.randomUUID().toString();
		long startNanos = System.nanoTime();
		boolean requestFailed = false;
		MDC.put("requestId", requestId);
		response.setHeader(REQUEST_ID_HEADER, requestId);

		try {
			chain.doFilter(request, response);
		} catch (IOException | ServletException | RuntimeException e) {
			requestFailed = true;
			long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
			log.error("HTTP request failed requestId={} method={} path={} durationMs={}", requestId, request.getMethod(),
					request.getRequestURI(), durationMs, e);
			throw e;
		} finally {
			if (!requestFailed) {
				long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
				Object principal = request.getAttribute("authenticatedUserId");
				String userId = principal instanceof String id ? id : "anonymous";
				int status = response.getStatus();
				if (status >= 500) {
					log.error("HTTP request completed requestId={} method={} path={} status={} durationMs={} user={}",
							requestId, request.getMethod(), request.getRequestURI(), status, durationMs, userId);
				} else if (status >= 400) {
					log.warn("HTTP request completed requestId={} method={} path={} status={} durationMs={} user={}",
							requestId, request.getMethod(), request.getRequestURI(), status, durationMs, userId);
				} else if (request.getRequestURI().startsWith("/api/")) {
					log.info("HTTP request completed requestId={} method={} path={} status={} durationMs={} user={}",
							requestId, request.getMethod(), request.getRequestURI(), status, durationMs, userId);
				}
			}
			MDC.remove("requestId");
		}
	}
}
