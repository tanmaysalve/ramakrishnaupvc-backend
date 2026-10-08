package com.ramkrishna.sales.controller;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UsersController {
	private final UserService service;

	public UsersController(UserService s) {
		service = s;
	}

	@GetMapping
	public List<UserDto> all() {
		return service.all();
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public UserDto create(@RequestBody UserRequest r) {
		return service.create(r);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public UserDto update(@PathVariable String id, @RequestBody UserRequest r) {
		return service.update(id, r);
	}
}