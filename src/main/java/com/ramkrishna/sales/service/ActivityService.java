package com.ramkrishna.sales.service;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.entity.*;
import com.ramkrishna.sales.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class ActivityService {
	private static final Logger log = LoggerFactory.getLogger(ActivityService.class);
	private final ActivityRepository activities;
	private final RequestRepository requests;
	private final UserRepository users;
	private final MapperService mapper;

	public ActivityService(ActivityRepository a, RequestRepository r, UserRepository u, MapperService m) {
		activities = a;
		requests = r;
		users = u;
		mapper = m;
	}

	public ActivityEntity add(RequestEntity r, String userId, Enums.ActivityType type, String description,
			String oldValue, String newValue, String doc) {
		UserEntity u = users.findById(userId).orElseThrow();
		ActivityEntity a = new ActivityEntity(UUID.randomUUID().toString(), r, u, type, description, oldValue, newValue,
				doc, Instant.now());
		r.touch();
		requests.save(r);
		ActivityEntity saved = activities.save(a);
		log.debug("Request activity recorded requestId={} activityId={} type={} userId={}", r.getId(), saved.getId(),
				type, userId);
		return saved;
	}

	@Transactional(readOnly = true)
	public List<ActivityDto> list(String requestId) {
		List<ActivityDto> result = activities.findByRequestIdOrderByCreatedAtDesc(requestId).stream()
				.map(mapper::activity).toList();
		log.debug("Listed request activities requestId={} count={}", requestId, result.size());
		return result;
	}

	@Transactional(readOnly = true)
	public List<ActivityDto> recent(int limit) {
		return activities
				.findAll(org.springframework.data.domain.PageRequest.of(0, limit,
						org.springframework.data.domain.Sort.by("createdAt").descending()))
				.stream().map(mapper::activity).toList();
	}
}
