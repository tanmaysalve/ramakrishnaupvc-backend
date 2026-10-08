package com.ramkrishna.sales.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sites")
public class SiteEntity {
	@Id
	private String id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id")
	private CustomerEntity customer;
	@Column(nullable = false)
	private String name;
	private String address;
	private String city;
	private String state;
	private String pincode;
	@Column(name = "contact_person")
	private String contactPerson;
	@Column(name = "contact_phone")
	private String contactPhone;
	private String notes;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public SiteEntity() {
	}

	public SiteEntity(String id, CustomerEntity customer, String name, String address, String city, String state,
			String pincode, String contactPerson, String contactPhone, String notes, Instant createdAt) {
		this.id = id;
		this.customer = customer;
		this.name = name;
		this.address = address;
		this.city = city;
		this.state = state;
		this.pincode = pincode;
		this.contactPerson = contactPerson;
		this.contactPhone = contactPhone;
		this.notes = notes;
		this.createdAt = createdAt;
	}

	public void update(CustomerEntity customer, String name, String address, String city, String state, String pincode,
			String contactPerson, String contactPhone, String notes) {
		this.customer = customer;
		this.name = name;
		this.address = address;
		this.city = city;
		this.state = state;
		this.pincode = pincode;
		this.contactPerson = contactPerson;
		this.contactPhone = contactPhone;
		this.notes = notes;
	}

	public String getId() {
		return id;
	}

	public CustomerEntity getCustomer() {
		return customer;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getPincode() {
		return pincode;
	}

	public String getContactPerson() {
		return contactPerson;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public String getNotes() {
		return notes;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
