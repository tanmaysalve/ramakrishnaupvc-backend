package com.ramkrishna.sales.exception;

import io.jsonwebtoken.JwtException;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	record ErrorBody(Instant timestamp, int status, String error, String message) {
	}

	@ExceptionHandler(Exceptions.NotFound.class)
	ResponseEntity<ErrorBody> notFound(Exceptions.NotFound e) {
		log.warn("Request returned 404: {}", e.getMessage());
		return body(HttpStatus.NOT_FOUND, e.getMessage());
	}

	@ExceptionHandler(Exceptions.Forbidden.class)
	ResponseEntity<ErrorBody> forbidden(Exceptions.Forbidden e) {
		log.warn("Request returned 403: {}", e.getMessage());
		return body(HttpStatus.FORBIDDEN, e.getMessage());
	}

	@ExceptionHandler(JwtException.class)
	ResponseEntity<ErrorBody> unauthorized(JwtException e) {
		log.warn("Request returned 401: invalid or expired JWT");
		return body(HttpStatus.UNAUTHORIZED, "Invalid or expired JWT");
	}

	@ExceptionHandler(Exceptions.BadRequest.class)
	ResponseEntity<ErrorBody> bad(Exceptions.BadRequest e) {
		log.warn("Request returned 400: {}", e.getMessage());
		return body(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
	ResponseEntity<ErrorBody> access(org.springframework.security.access.AccessDeniedException e) {
		log.warn("Spring Security denied access");
		return body(HttpStatus.FORBIDDEN, e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorBody> validation(MethodArgumentNotValidException e) {
		String m = e.getBindingResult().getFieldErrors().stream().map(x -> x.getField() + ": " + x.getDefaultMessage())
				.findFirst().orElse("Validation failed");
		log.warn("Request validation failed: {}", m);
		return body(HttpStatus.BAD_REQUEST, m);
	}

	private ResponseEntity<ErrorBody> body(HttpStatus s, String m) {
		return ResponseEntity.status(s).body(new ErrorBody(Instant.now(), s.value(), s.getReasonPhrase(), m));
	}
}
