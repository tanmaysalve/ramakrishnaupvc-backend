package com.ramkrishna.sales.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ramkrishna.sales.dto.ApiDtos.CustomerDto;
import com.ramkrishna.sales.dto.ApiDtos.CustomerRequest;
import com.ramkrishna.sales.dto.ApiDtos.SiteDto;
import com.ramkrishna.sales.dto.ApiDtos.SiteRequest;
import com.ramkrishna.sales.entity.CustomerEntity;
import com.ramkrishna.sales.entity.Enums.CustomerType;
import com.ramkrishna.sales.entity.SiteEntity;
import com.ramkrishna.sales.exception.Exceptions;
import com.ramkrishna.sales.repository.CustomerRepository;
import com.ramkrishna.sales.repository.SiteRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class MasterDataListServiceTest {
	@Mock
	private CustomerRepository customers;
	@Mock
	private SiteRepository sites;
	@Mock
	private MapperService mapper;

	private CustomerService customerService;
	private SiteService siteService;

	@BeforeEach
	void setUp() {
		customerService = new CustomerService(customers, mapper);
		siteService = new SiteService(sites, customers, mapper);
	}

	@Test
	@SuppressWarnings("unchecked")
	void customerSearchTrimsTermAndUsesCaseInsensitivePartialMatch() {
		when(customers.findAll(ArgumentMatchers.<Specification<CustomerEntity>>any(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 50), 0));

		customerService.all("  AcMe  ", 1, 50);

		ArgumentCaptor<Specification<CustomerEntity>> spec = ArgumentCaptor.forClass(Specification.class);
		verify(customers).findAll(spec.capture(), any(Pageable.class));
		Root<CustomerEntity> root = mock(Root.class);
		Path<String> name = mock(Path.class);
		Expression<String> loweredName = mock(Expression.class);
		CriteriaBuilder criteria = mock(CriteriaBuilder.class);
		Predicate expected = mock(Predicate.class);
		when(root.<String>get("name")).thenReturn(name);
		when(criteria.lower(name)).thenReturn(loweredName);
		when(criteria.like(loweredName, "%acme%", '\\')).thenReturn(expected);

		assertSame(expected, spec.getValue().toPredicate(root, null, criteria));
		verify(criteria).like(loweredName, "%acme%", '\\');
	}

	@Test
	@SuppressWarnings("unchecked")
	void customerListCapsLimitAndKeepsOutOfRangePageEmpty() {
		PageRequest expectedPage = PageRequest.of(3, 50, org.springframework.data.domain.Sort.by("name").ascending()
				.and(org.springframework.data.domain.Sort.by("id").ascending()));
		Page<CustomerEntity> result = mock(Page.class);
		Page<CustomerDto> mapped = mock(Page.class);
		when(customers.findAll(ArgumentMatchers.<Specification<CustomerEntity>>any(), any(Pageable.class)))
				.thenReturn(result);
		doReturn(mapped).when(result).map(any());
		when(mapped.getContent()).thenReturn(List.of());
		when(mapped.getNumber()).thenReturn(3);
		when(mapped.getSize()).thenReturn(50);
		when(mapped.getTotalElements()).thenReturn(105L);
		when(mapped.getTotalPages()).thenReturn(3);

		var response = customerService.all(null, 4, 500);

		assertTrue(response.data().isEmpty());
		assertEquals(4, response.pagination().page());
		assertEquals(50, response.pagination().limit());
		assertEquals(105, response.pagination().total());
		assertEquals(3, response.pagination().totalPages());
		ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
		verify(customers).findAll(ArgumentMatchers.<Specification<CustomerEntity>>any(), pageable.capture());
		assertEquals(expectedPage, pageable.getValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	void siteSearchCombinesCustomerFilterAndNamePattern() {
		when(sites.findAll(ArgumentMatchers.<Specification<SiteEntity>>any(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 50), 0));

		siteService.all("customer-1", "  North  ", 1, 50);

		ArgumentCaptor<Specification<SiteEntity>> spec = ArgumentCaptor.forClass(Specification.class);
		verify(sites).findAll(spec.capture(), any(Pageable.class));
		Root<SiteEntity> root = mock(Root.class);
		Path<Object> customerPath = mock(Path.class);
		Path<Object> customerId = mock(Path.class);
		Path<String> name = mock(Path.class);
		Expression<String> loweredName = mock(Expression.class);
		Predicate customerPredicate = mock(Predicate.class);
		Predicate namePredicate = mock(Predicate.class);
		Predicate combined = mock(Predicate.class);
		AtomicReference<Predicate[]> appliedPredicates = new AtomicReference<>();
		CriteriaBuilder criteria = mock(CriteriaBuilder.class, invocation -> {
			if ("and".equals(invocation.getMethod().getName())) {
				appliedPredicates.set(invocation.getArgument(0));
				return combined;
			}
			return org.mockito.Answers.RETURNS_DEFAULTS.answer(invocation);
		});
		when(root.get("customer")).thenReturn(customerPath);
		when(customerPath.get("id")).thenReturn(customerId);
		when(root.<String>get("name")).thenReturn(name);
		when(criteria.equal(customerId, "customer-1")).thenReturn(customerPredicate);
		when(criteria.lower(name)).thenReturn(loweredName);
		when(criteria.like(loweredName, "%north%", '\\')).thenReturn(namePredicate);

		assertSame(combined, spec.getValue().toPredicate(root, null, criteria));
		verify(criteria).equal(customerId, "customer-1");
		verify(criteria).like(loweredName, "%north%", '\\');
		assertEquals(2, appliedPredicates.get().length);
	}

	@Test
	void rejectsInvalidPageAndLimit() {
		assertThrows(Exceptions.BadRequest.class, () -> customerService.all(null, 0, 50));
		assertThrows(Exceptions.BadRequest.class, () -> siteService.all(null, null, 1, 0));
		verifyNoInteractions(customers, sites);
	}

	@Test
	void customerCreateAndUpdateStillPersistRequestedValues() {
		CustomerEntity existing = customer("customer-1", "Old name");
		when(customers.findById("customer-1")).thenReturn(Optional.of(existing));
		when(customers.save(any(CustomerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(mapper.customer(any(CustomerEntity.class))).thenReturn(new CustomerDto("customer-1", "name", null,
				CustomerType.RETAIL_CUSTOMER, "phone", null, null, null, null, null, null, Instant.now()));

		customerService.create(customerRequest("Created"));
		customerService.update("customer-1", customerRequest("Updated"));

		ArgumentCaptor<CustomerEntity> saved = ArgumentCaptor.forClass(CustomerEntity.class);
		verify(customers, times(2)).save(saved.capture());
		assertEquals("Created", saved.getAllValues().get(0).getName());
		assertEquals("Updated", saved.getAllValues().get(1).getName());
		assertEquals("phone", saved.getAllValues().get(1).getPhone());
	}

	@Test
	void siteCreateAndUpdateStillPersistRequestedValues() {
		CustomerEntity customer = customer("customer-1", "Customer");
		when(customers.findById("customer-1")).thenReturn(Optional.of(customer));
		when(sites.findById("site-1")).thenReturn(Optional.of(site("site-1", customer, "Old site")));
		when(sites.save(any(SiteEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(mapper.site(any(SiteEntity.class))).thenReturn(
				new SiteDto("site-1", "customer-1", "site", null, null, null, null, null, null, null, Instant.now()));

		siteService.create(siteRequest("Created"));
		siteService.update("site-1", siteRequest("Updated"));

		ArgumentCaptor<SiteEntity> saved = ArgumentCaptor.forClass(SiteEntity.class);
		verify(sites, times(2)).save(saved.capture());
		assertEquals("Created", saved.getAllValues().get(0).getName());
		assertEquals("Updated", saved.getAllValues().get(1).getName());
		assertEquals(customer, saved.getAllValues().get(1).getCustomer());
	}

	private static CustomerEntity customer(String id, String name) {
		return new CustomerEntity(id, name, null, CustomerType.RETAIL_CUSTOMER, "phone", null, null, null, null, null,
				null, Instant.now());
	}

	private static SiteEntity site(String id, CustomerEntity customer, String name) {
		return new SiteEntity(id, customer, name, null, null, null, null, null, null, null, Instant.now());
	}

	private static CustomerRequest customerRequest(String name) {
		return new CustomerRequest(name, null, CustomerType.RETAIL_CUSTOMER, "phone", null, null, null, null, null,
				null);
	}

	private static SiteRequest siteRequest(String name) {
		return new SiteRequest("customer-1", name, null, null, null, null, null, null, null);
	}
}
