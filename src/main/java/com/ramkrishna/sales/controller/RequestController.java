package com.ramkrishna.sales.controller;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.entity.Enums.Priority;
import com.ramkrishna.sales.entity.Enums.RequestSource;
import com.ramkrishna.sales.entity.Enums.RequestStatus;
import com.ramkrishna.sales.security.AuthenticatedUser;
import com.ramkrishna.sales.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
	private final RequestService requests;
	private final ActivityService activities;
	private final DocumentService documents;

	public RequestController(RequestService r, ActivityService a, DocumentService d) {
		requests = r;
		activities = a;
		documents = d;
	}

	@GetMapping
	public RequestPageDto all(@AuthenticationPrincipal AuthenticatedUser u,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) RequestStatus status,
			@RequestParam(required = false) Priority priority,
			@RequestParam(required = false) RequestSource source,
			@RequestParam(required = false) String assignedTo) {
		return requests.all(u, page, size, search, status, priority, source, assignedTo);
	}

	@GetMapping("/{id}")
	public RequestDto get(@PathVariable String id, @AuthenticationPrincipal AuthenticatedUser u) {
		return requests.dto(id, u);
	}

	@PostMapping
	public RequestDto create(@Valid @RequestBody RequestCreateRequest d, @AuthenticationPrincipal AuthenticatedUser u) {
		return requests.create(d, u);
	}

	@PutMapping("/{id}")
	public RequestDto update(@PathVariable String id, @Valid @RequestBody RequestUpdateRequest d,
			@AuthenticationPrincipal AuthenticatedUser u) {
		return requests.update(id, d, u);
	}

	@PatchMapping("/{id}/status")
	public RequestDto status(@PathVariable String id, @Valid @RequestBody StatusRequest d,
			@AuthenticationPrincipal AuthenticatedUser u) {
		return requests.status(id, d, u);
	}

	@PatchMapping("/{id}/assignment")
	public RequestDto assign(@PathVariable String id, @Valid @RequestBody AssignmentRequest d,
			@AuthenticationPrincipal AuthenticatedUser u) {
		return requests.assign(id, d, u);
	}

	@PostMapping("/{id}/comments")
	public ActivityDto comment(@PathVariable String id, @Valid @RequestBody CommentRequest d,
			@AuthenticationPrincipal AuthenticatedUser u) {
		return requests.comment(id, d, u);
	}

	@GetMapping("/{id}/activities")
	public List<ActivityDto> activities(@PathVariable String id, @AuthenticationPrincipal AuthenticatedUser u) {
		requests.visible(id, u);
		return activities.list(id);
	}

	@GetMapping("/{id}/documents")
	public List<DocumentDto> docs(@PathVariable String id, @AuthenticationPrincipal AuthenticatedUser u) {
		return documents.list(id, u);
	}
}
