package com.ramkrishna.sales.service;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.entity.UserEntity;
import com.ramkrishna.sales.exception.Exceptions;
import com.ramkrishna.sales.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;

@Service
public class UserService {
	private static final Logger log = LoggerFactory.getLogger(UserService.class);
	private final UserRepository repo;
	private final MapperService mapper;
	private final PasswordEncoder encoder;

	public UserService(UserRepository r, MapperService m, PasswordEncoder e) {
		repo = r;
		mapper = m;
		encoder = e;
	}

	public List<UserDto> all() {
		return repo.findAllByOrderByDisplayNameAsc().stream().map(mapper::user).toList();
	}

	public UserDto create(UserRequest r) {
		if (r.username() == null || r.username().isBlank() || r.displayName() == null || r.displayName().isBlank()
				|| r.role() == null || r.password() == null || r.password().length() < 8)
			throw new Exceptions.BadRequest(
					"Username, display name, role and password of at least 8 characters are required");
		if (repo.findByUsername(r.username().trim()).isPresent())
			throw new Exceptions.BadRequest("Username already exists");
		UserEntity u = new UserEntity(UUID.randomUUID().toString(), r.username().trim(), encoder.encode(r.password()),
				r.displayName().trim(), r.role(), r.active() == null || r.active(), Instant.now());
		UserDto created = mapper.user(repo.save(u));
		log.info("User created userId={} role={} active={}", created.id(), created.role(), created.active());
		return created;
	}

	@SuppressWarnings("unused")
	public UserDto update(String id, UserRequest r) {
		UserEntity u = repo.findById(id).orElseThrow(() -> new Exceptions.NotFound("User not found"));
		if (r.username() == null || r.username().isBlank() || r.displayName() == null || r.displayName().isBlank()
				|| r.role() == null)
			throw new Exceptions.BadRequest("Username, display name and role are required");
		repo.findByUsername(r.username().trim()).filter(x -> !x.getId().equals(id)).ifPresent(x -> {
			throw new Exceptions.BadRequest("Username already exists");
		});
		String hash = (r.password() == null || r.password().isBlank()) ? null : encoder.encode(r.password());
		u.update(r.username().trim(), r.displayName().trim(), r.role(), r.active() == null ? u.isActive() : r.active(),
				hash);
		UserDto updated = mapper.user(repo.save(u));
		log.info("User updated userId={} role={} active={}", updated.id(), updated.role(), updated.active());
		return updated;
	}
}