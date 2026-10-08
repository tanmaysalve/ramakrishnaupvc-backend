package com.ramkrishna.sales.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "requests")
public class RequestEntity {
	@Id
	private String id;
	@Column(name = "request_number", nullable = false, unique = true)
	private String requestNumber;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id")
	private CustomerEntity customer;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_id")
	private SiteEntity site;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by")
	private UserEntity createdBy;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_to")
	private UserEntity assignedTo;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Enums.RequestSource source;
	@Enumerated(EnumType.STRING)
	@Column(name = "request_type", nullable = false)
	private Enums.RequestType requestType;
	@Enumerated(EnumType.STRING)
	@Column(name = "product_category")
	private Enums.ProductCategory productCategory;
	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;
	private Integer quantity;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Enums.Priority priority;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Enums.RequestStatus status;
	@Column(name = "expected_date")
	private LocalDate expectedDate;
	@Column(columnDefinition = "TEXT")
	private String remarks;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public RequestEntity() {
	}

	public String getId() {
		return id;
	}

	public String getRequestNumber() {
		return requestNumber;
	}

	public CustomerEntity getCustomer() {
		return customer;
	}

	public SiteEntity getSite() {
		return site;
	}

	public UserEntity getCreatedBy() {
		return createdBy;
	}

	public UserEntity getAssignedTo() {
		return assignedTo;
	}

	public Enums.RequestSource getSource() {
		return source;
	}

	public Enums.RequestType getRequestType() {
		return requestType;
	}

	public Enums.ProductCategory getProductCategory() {
		return productCategory;
	}

	public String getDescription() {
		return description;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public Enums.Priority getPriority() {
		return priority;
	}

	public Enums.RequestStatus getStatus() {
		return status;
	}

	public LocalDate getExpectedDate() {
		return expectedDate;
	}

	public String getRemarks() {
		return remarks;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setInitial(String id, String num, CustomerEntity customer, SiteEntity site, UserEntity createdBy,
			UserEntity assignedTo, Enums.RequestSource source, Enums.RequestType type, Enums.ProductCategory product,
			String description, Integer quantity, Enums.Priority priority, Enums.RequestStatus status,
			LocalDate expectedDate, String remarks, Instant now) {
		this.id = id;
		this.requestNumber = num;
		this.customer = customer;
		this.site = site;
		this.createdBy = createdBy;
		this.assignedTo = assignedTo;
		this.source = source;
		this.requestType = type;
		this.productCategory = product;
		this.description = description;
		this.quantity = quantity;
		this.priority = priority;
		this.status = status;
		this.expectedDate = expectedDate;
		this.remarks = remarks;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public void update(String description, Enums.Priority priority, LocalDate expectedDate, String remarks,
			Enums.RequestSource source, Enums.RequestType type, Enums.ProductCategory product, Integer quantity) {
		this.description = description;
		this.priority = priority;
		this.expectedDate = expectedDate;
		this.remarks = remarks;
		this.source = source;
		this.requestType = type;
		this.productCategory = product;
		this.quantity = quantity;
		this.updatedAt = Instant.now();
	}

	public void touch() {
		this.updatedAt = Instant.now();
	}

	public void changeStatus(Enums.RequestStatus s) {
		this.status = s;
		this.updatedAt = Instant.now();
	}

	public void assign(UserEntity u) {
		this.assignedTo = u;
		this.updatedAt = Instant.now();
	}
}
