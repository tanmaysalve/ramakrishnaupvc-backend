package com.ramkrishna.sales.service;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.entity.UserEntity;
import com.ramkrishna.sales.exception.Exceptions;
import com.ramkrishna.sales.repository.UserRepository;
import com.ramkrishna.sales.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	private static final Logger log = LoggerFactory.getLogger(AuthService.class);
	private final UserRepository users;
	private final PasswordEncoder encoder;
	private final JwtService jwt;
	private final MapperService mapper;

	public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt, MapperService mapper) {
		this.users = users;
		this.encoder = encoder;
		this.jwt = jwt;
		this.mapper = mapper;
	}

	public LoginResponse login(LoginRequest r) {
		UserEntity u = users.findByUsername(r.username())
				.orElse(null);
		if (u == null || !u.isActive() || !encoder.matches(r.password(), u.getPasswordHash())) {
			log.warn("Login rejected");
			throw new Exceptions.BadRequest("Invalid username or password");
		}
		var principal = new AuthenticatedUser(u.getId(), u.getUsername(), u.getDisplayName(), u.getRole());
		log.info("Login succeeded userId={} role={}", u.getId(), u.getRole());
		return new LoginResponse(jwt.generate(principal), mapper.user(u));
	}

	public UserDto me(AuthenticatedUser p) {
		log.debug("Current-user profile requested userId={}", p.id());
		return mapper.user(users.findById(p.id()).orElseThrow(() -> new Exceptions.NotFound("User not found")));
	}
}
