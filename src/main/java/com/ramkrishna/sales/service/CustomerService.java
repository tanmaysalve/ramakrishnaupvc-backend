package com.ramkrishna.sales.service;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.entity.CustomerEntity;
import com.ramkrishna.sales.exception.Exceptions;
import com.ramkrishna.sales.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CustomerService {
	private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
	private final CustomerRepository repo;
	private final MapperService mapper;

	public CustomerService(CustomerRepository r, MapperService m) {
		repo = r;
		mapper = m;
	}

	@Transactional(readOnly = true)
	public ListPageDto<CustomerDto> all(String search, int page, int limit) {
		String normalizedSearch = ListPagination.normalizeSearch(search);
		Specification<CustomerEntity> spec = (root, query, criteriaBuilder) -> normalizedSearch == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.like(criteriaBuilder.lower(root.<String>get("name")),
						"%" + normalizedSearch + "%", '\\');
		var result = repo.findAll(spec, ListPagination.pageRequest(page, limit));
		log.debug("Listed customers page={} limit={} total={}", page, result.getSize(), result.getTotalElements());
		return ListPagination.response(result, mapper::customer);
	}

	public CustomerEntity get(String id) {
		return repo.findById(id).orElseThrow(() -> new Exceptions.NotFound("Customer not found"));
	}

	public CustomerDto create(CustomerRequest r) {
		if (r.name() == null || r.name().isBlank() || r.phone() == null || r.phone().isBlank()
				|| r.customerType() == null)
			throw new Exceptions.BadRequest("Name, phone and customer type are required");
		CustomerEntity c = new CustomerEntity(UUID.randomUUID().toString(), r.name(), r.companyName(), r.customerType(),
				r.phone(), r.alternatePhone(), r.email(), r.address(), r.city(), r.state(), r.pincode(), Instant.now());
		CustomerDto created = mapper.customer(repo.save(c));
		log.info("Customer created customerId={} type={}", created.id(), created.customerType());
		return created;
	}

	public CustomerDto update(String id, CustomerRequest r) {
		CustomerEntity c = get(id);
		c.update(r.name(), r.companyName(), r.customerType(), r.phone(), r.alternatePhone(), r.email(), r.address(),
				r.city(), r.state(), r.pincode());
		CustomerDto updated = mapper.customer(repo.save(c));
		log.info("Customer updated customerId={} type={}", id, updated.customerType());
		return updated;
	}
}
