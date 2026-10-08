package com.ramkrishna.sales.controller;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.service.SiteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/sites")
public class SiteController {
	private final SiteService service;

	public SiteController(SiteService s) {
		service = s;
	}

	@GetMapping
	public ListPageDto<SiteDto> all(@RequestParam(required = false) String customerId,
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "50") int limit) {
		return service.all(customerId, search, page, limit);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public SiteDto create(@Valid @RequestBody SiteRequest r) {
		return service.create(r);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public SiteDto update(@PathVariable String id, @Valid @RequestBody SiteRequest r) {
		return service.update(id, r);
	}
}
