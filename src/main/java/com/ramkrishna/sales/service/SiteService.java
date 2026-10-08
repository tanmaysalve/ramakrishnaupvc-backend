package com.ramkrishna.sales.service;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.entity.CustomerEntity;
import com.ramkrishna.sales.entity.SiteEntity;
import com.ramkrishna.sales.exception.Exceptions;
import com.ramkrishna.sales.repository.CustomerRepository;
import com.ramkrishna.sales.repository.SiteRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class SiteService {
	private static final Logger log = LoggerFactory.getLogger(SiteService.class);
	private final SiteRepository repo;
	private final CustomerRepository customers;
	private final MapperService mapper;

	public SiteService(SiteRepository r, CustomerRepository c, MapperService m) {
		repo = r;
		customers = c;
		mapper = m;
	}

	@Transactional(readOnly = true)
	public ListPageDto<SiteDto> all(String customerId, String search, int page, int limit) {
		String normalizedSearch = ListPagination.normalizeSearch(search);
		Specification<SiteEntity> spec = (root, query, criteriaBuilder) -> {
			var predicates = new ArrayList<Predicate>();
			if (customerId != null)
				predicates.add(criteriaBuilder.equal(root.get("customer").get("id"), customerId));
			if (normalizedSearch != null)
				predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.<String>get("name")),
						"%" + normalizedSearch + "%", '\\'));
			return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
		};
		var result = repo.findAll(spec, ListPagination.pageRequest(page, limit));
		log.debug("Listed sites customerId={} page={} limit={} total={}", customerId == null ? "all" : customerId, page,
				result.getSize(), result.getTotalElements());
		return ListPagination.response(result, mapper::site);
	}

	public SiteEntity get(String id) {
		return repo.findById(id).orElseThrow(() -> new Exceptions.NotFound("Site not found"));
	}

	public SiteDto create(SiteRequest r) {
		CustomerEntity c = customers.findById(r.customerId())
				.orElseThrow(() -> new Exceptions.NotFound("Customer not found"));
		if (r.name() == null || r.name().isBlank())
			throw new Exceptions.BadRequest("Site name is required");
		SiteEntity s = new SiteEntity(UUID.randomUUID().toString(), c, r.name(), r.address(), r.city(), r.state(),
				r.pincode(), r.contactPerson(), r.contactPhone(), r.notes(), Instant.now());
		SiteDto created = mapper.site(repo.save(s));
		log.info("Site created siteId={} customerId={}", created.id(), r.customerId());
		return created;
	}

	public SiteDto update(String id, SiteRequest r) {
		SiteEntity s = get(id);
		CustomerEntity c = customers.findById(r.customerId())
				.orElseThrow(() -> new Exceptions.NotFound("Customer not found"));
		s.update(c, r.name(), r.address(), r.city(), r.state(), r.pincode(), r.contactPerson(), r.contactPhone(),
				r.notes());
		SiteDto updated = mapper.site(repo.save(s));
		log.info("Site updated siteId={} customerId={}", id, r.customerId());
		return updated;
	}
}
