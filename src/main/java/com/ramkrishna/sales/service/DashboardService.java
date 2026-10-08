package com.ramkrishna.sales.service;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.entity.*;
import com.ramkrishna.sales.repository.*;
import com.ramkrishna.sales.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class DashboardService {
	private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
	private final RequestRepository requests;
	private final ActivityRepository activities;
	private final UserRepository users;
	private final MapperService mapper;

	public DashboardService(RequestRepository r, ActivityRepository a, UserRepository u, MapperService m) {
		requests = r;
		activities = a;
		users = u;
		this.mapper = new MapperService();
	}

	@SuppressWarnings("unused")
	@Transactional(readOnly = true)
	public DashboardDto dashboard(AuthenticatedUser u) {
		List<RequestEntity> rs = u.role() == Enums.Role.ADMIN ? requests.findAllByOrderByUpdatedAtDesc()
				: requests.findByCreatedByIdOrAssignedToIdOrderByUpdatedAtDesc(u.id(), u.id());
		Map<Enums.RequestStatus, Long> c = new EnumMap<>(Enums.RequestStatus.class);
		for (Enums.RequestStatus s : Enums.RequestStatus.values())
			c.put(s, 0L);
		rs.forEach(r -> c.compute(r.getStatus(), (k, v) -> v + 1));
		Map<String, Long> byUser = new LinkedHashMap<>();
		users.findAllByOrderByDisplayNameAsc().stream().filter(x -> x.getRole() == Enums.Role.SALES_USER)
				.forEach(x -> byUser.put(x.getDisplayName(),
						rs.stream()
								.filter(r -> r.getAssignedTo() != null && r.getAssignedTo().getId().equals(x.getId()))
								.count()));
		Map<String, Long> bySource = new LinkedHashMap<>();
		rs.forEach(r -> bySource.put(r.getSource().name(), bySource.getOrDefault(r.getSource().name(), 0L) + 1));
		List<RequestDto> recent = rs.stream().limit(8).map(mapper::request).toList();
		List<ActivityDto> recentActs = activities
				.findAll(org.springframework.data.domain.PageRequest.of(0, 10,
						org.springframework.data.domain.Sort.by("createdAt").descending()))
				.stream().map(mapper::activity).toList();
		long attention = rs.stream()
				.filter(r -> r.getPriority() == Enums.Priority.HIGH || r.getPriority() == Enums.Priority.URGENT)
				.count();
		log.debug("Dashboard generated userId={} role={} requestCount={} attentionCount={}", u.id(), u.role(),
				rs.size(), attention);
		return new DashboardDto(rs.size(), c.get(Enums.RequestStatus.NEW), c.get(Enums.RequestStatus.CONTACTED),
				c.get(Enums.RequestStatus.SITE_VISIT), c.get(Enums.RequestStatus.REQUIREMENTS),
				c.get(Enums.RequestStatus.FOLLOW_UP), c.get(Enums.RequestStatus.QUOTATION_PENDING),
				c.get(Enums.RequestStatus.CONFIRMED), c.get(Enums.RequestStatus.IN_PROGRESS),
				c.get(Enums.RequestStatus.COMPLETED), c.get(Enums.RequestStatus.CANCELLED), byUser, bySource, recent,
				recentActs, attention);
	}
}
