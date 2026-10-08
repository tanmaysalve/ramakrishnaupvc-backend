package com.ramkrishna.sales.controller;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.security.AuthenticatedUser;
import com.ramkrishna.sales.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService service;

	public AuthController(AuthService s) {
		service = s;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest r) {
		return service.login(r);
	}

	@GetMapping("/me")
	public UserDto me(@AuthenticationPrincipal AuthenticatedUser u) {
		return service.me(u);
	}
}
