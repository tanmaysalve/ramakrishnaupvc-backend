package com.ramkrishna.sales.service;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.entity.*;
import com.ramkrishna.sales.exception.Exceptions;
import com.ramkrishna.sales.repository.*;
import com.ramkrishna.sales.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class RequestService {
	private static final Logger log = LoggerFactory.getLogger(RequestService.class);
	private final RequestRepository repo;
	private final CustomerRepository customers;
	private final SiteRepository sites;
	private final UserRepository users;
	private final ActivityService activity;
	private final MapperService mapper;

	public RequestService(RequestRepository r, CustomerRepository c, SiteRepository s, UserRepository u,
			ActivityService a, MapperService m) {
		repo = r;
		customers = c;
		sites = s;
		users = u;
		activity = a;
		mapper = m;
	}

	@Transactional(readOnly = true)
	public RequestPageDto all(AuthenticatedUser u, int page, int size, String search, Enums.RequestStatus status,
			Enums.Priority priority, Enums.RequestSource source, String assignedTo) {
		if (page < 0)
			throw new Exceptions.BadRequest("Page must be zero or greater");
		if (size < 1 || size > 100)
			throw new Exceptions.BadRequest("Page size must be between 1 and 100");
		if (search != null && search.length() > 100)
			throw new Exceptions.BadRequest("Search must be 100 characters or fewer");
		if (assignedTo != null && assignedTo.isBlank())
			throw new Exceptions.BadRequest("Assignee must be a user ID or 'unassigned'");

		String normalizedSearch = search == null || search.isBlank() ? "" : search.trim();
		boolean unassigned = "unassigned".equalsIgnoreCase(assignedTo);
		String normalizedAssignee = assignedTo == null || unassigned ? null : assignedTo.trim();
		var pageable = PageRequest.of(page, size,
				Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id")));
		Page<RequestEntity> result = repo.findVisibleRequests(u.role() == Enums.Role.ADMIN, u.id(), normalizedSearch,
				status, priority, source, normalizedAssignee, unassigned, pageable);
		log.debug("Listed requests userId={} role={} page={} size={} count={}", u.id(), u.role(), page, size,
				result.getNumberOfElements());
		return new RequestPageDto(result.map(mapper::request).getContent(), result.getNumber(), result.getSize(),
				result.getTotalElements(), result.getTotalPages(), result.isFirst(), result.isLast(),
				result.hasNext(), result.hasPrevious());
	}

	public RequestDto dto(String id, AuthenticatedUser u) {
		return mapper.request(visible(id, u));
	}

	public RequestEntity getEntity(String id) {
		return repo.findById(id).orElseThrow(() -> new Exceptions.NotFound("Request not found"));
	}

	public RequestEntity visible(String id, AuthenticatedUser u) {
		RequestEntity r = getEntity(id);
		if (u.role() != Enums.Role.ADMIN && !r.getCreatedBy().getId().equals(u.id())
				&& (r.getAssignedTo() == null || !r.getAssignedTo().getId().equals(u.id()))) {
			log.warn("Request access denied userId={} requestId={}", u.id(), id);
			throw new Exceptions.Forbidden("You are not allowed to access this request");
		}
		return r;
	}

	@Transactional
	public RequestDto create(RequestCreateRequest d, AuthenticatedUser u) {
		CustomerEntity c = customers.findById(d.customerId())
				.orElseThrow(() -> new Exceptions.NotFound("Customer not found"));
		SiteEntity s = d.siteId() == null || d.siteId().isBlank() ? null
				: sites.findById(d.siteId()).orElseThrow(() -> new Exceptions.NotFound("Site not found"));
		UserEntity ass = null;
		if (u.role() == Enums.Role.ADMIN && d.assignedTo() != null && !d.assignedTo().isBlank())
			ass = users.findById(d.assignedTo()).orElseThrow(() -> new Exceptions.NotFound("Assigned user not found"));
		else if (u.role() == Enums.Role.SALES_USER)
			ass = users.findById(u.id()).orElseThrow();
		if (d.description() == null || d.description().isBlank() || d.source() == null || d.requestType() == null
				|| d.priority() == null)
			throw new Exceptions.BadRequest("Source, request type, priority and description are required");
		long seq = repo.nextRequestSequence();
		String number = "RK-" + Year.now().getValue() + "-" + String.format("%05d", seq);
		RequestEntity r = new RequestEntity();
		Instant now = Instant.now();
		r.setInitial(UUID.randomUUID().toString(), number, c, s, users.findById(u.id()).orElseThrow(), ass, d.source(),
				d.requestType(), d.productCategory(), d.description().trim(), d.quantity(), d.priority(),
				Enums.RequestStatus.NEW, d.expectedDate(), d.remarks(), now);
		repo.save(r);
		activity.add(r, u.id(), Enums.ActivityType.REQUEST_CREATED, "created this request.", null, null, null);
		log.info("Request created requestId={} requestNumber={} userId={} assignedTo={}", r.getId(), number, u.id(),
				ass == null ? "unassigned" : ass.getId());
		return mapper.request(r);
	}

	public RequestDto update(String id, RequestUpdateRequest d, AuthenticatedUser u) {
		RequestEntity r = visible(id, u);
		if (d.description() == null || d.description().isBlank() || d.source() == null || d.requestType() == null
				|| d.priority() == null)
			throw new Exceptions.BadRequest("Source, request type, priority and description are required");
		Enums.Priority oldPriority = r.getPriority();
		r.update(d.description(), d.priority(), d.expectedDate(), d.remarks(), d.source(), d.requestType(),
				d.productCategory(), d.quantity());
		repo.save(r);
		activity.add(r, u.id(), Enums.ActivityType.REQUEST_UPDATED, "updated request details.", null, null, null);
		if (oldPriority != r.getPriority())
			activity.add(r, u.id(), Enums.ActivityType.PRIORITY_CHANGED,
					"changed priority: " + oldPriority + " → " + r.getPriority(), oldPriority.name(),
					r.getPriority().name(), null);
		log.info("Request updated requestId={} userId={} priority={}", id, u.id(), r.getPriority());
		return mapper.request(r);
	}

	public RequestDto status(String id, StatusRequest d, AuthenticatedUser u) {
		RequestEntity r = visible(id, u);
		if (d.status() == null)
			throw new Exceptions.BadRequest("Status is required");
		Enums.RequestStatus old = r.getStatus();
		r.changeStatus(d.status());
		repo.save(r);
		activity.add(r, u.id(), Enums.ActivityType.STATUS_CHANGED,
				"changed status: " + old.name().replace('_', ' ') + " → " + d.status().name().replace('_', ' '),
				old.name(), d.status().name(), null);
		log.info("Request status changed requestId={} userId={} from={} to={}", id, u.id(), old, d.status());
		return mapper.request(r);
	}

	@Transactional
	public RequestDto assign(String id, AssignmentRequest d, AuthenticatedUser u) {
		if (u.role() != Enums.Role.ADMIN) {
			log.warn("Request assignment denied userId={} requestId={} role={}", u.id(), id, u.role());
			throw new Exceptions.Forbidden("Only administrators can assign requests");
		}
		RequestEntity r = repo.findByIdWithAssignedTo(id)
				.orElseThrow(() -> new Exceptions.NotFound("Request not found"));
		UserEntity target = users.findById(d.assignedTo())
				.orElseThrow(() -> new Exceptions.NotFound("Sales user not found"));
		if (target.getRole() != Enums.Role.SALES_USER)
			throw new Exceptions.BadRequest("Requests can only be assigned to sales users");
		String old = r.getAssignedTo() == null ? null : r.getAssignedTo().getDisplayName();
		r.assign(target);
		repo.save(r);
		activity.add(r, u.id(), old == null ? Enums.ActivityType.ASSIGNED : Enums.ActivityType.REASSIGNED,
				(old == null ? "assigned" : "reassigned") + " this request: " + (old == null ? "Unassigned" : old)
						+ " → " + target.getDisplayName(),
				old, target.getDisplayName(), null);
		log.info("Request assigned requestId={} adminUserId={} previousAssignment={} assignedTo={}", id, u.id(),
				old == null ? "unassigned" : "assigned", target.getId());
		return mapper.request(r);
	}

	@Transactional
	public ActivityDto comment(String id, CommentRequest d, AuthenticatedUser u) {
		RequestEntity r = visible(id, u);
		if (d.text() == null || d.text().isBlank())
			throw new Exceptions.BadRequest("Comment cannot be empty");
		log.info("Request comment added requestId={} userId={}", id, u.id());
		return mapper.activity(activity.add(r, u.id(), Enums.ActivityType.COMMENT,
				"commented: “" + d.text().trim() + "”", null, null, null));
	}
}
