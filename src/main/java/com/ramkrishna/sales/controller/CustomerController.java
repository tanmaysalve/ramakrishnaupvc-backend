package com.ramkrishna.sales.controller;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
	private final CustomerService service;

	public CustomerController(CustomerService s) {
		service = s;
	}

	@GetMapping
	public ListPageDto<CustomerDto> all(@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "50") int limit) {
		return service.all(search, page, limit);
	}

	@PostMapping
//	@PreAuthorize("hasRole('ADMIN')")
	public CustomerDto create(@Valid @RequestBody CustomerRequest r) {
		return service.create(r);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public CustomerDto update(@PathVariable String id, @Valid @RequestBody CustomerRequest r) {
		return service.update(id, r);
	}
}
