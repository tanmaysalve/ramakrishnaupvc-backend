package com.ramkrishna.sales.controller;

import com.ramkrishna.sales.dto.ApiDtos.DashboardDto;
import com.ramkrishna.sales.security.AuthenticatedUser;
import com.ramkrishna.sales.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
	private final DashboardService service;

	public DashboardController(DashboardService s) {
		service = s;
	}

	@GetMapping
	public DashboardDto dashboard(@AuthenticationPrincipal AuthenticatedUser u) {
		return service.dashboard(u);
	}
}
