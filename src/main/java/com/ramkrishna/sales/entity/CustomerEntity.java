package com.ramkrishna.sales.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "customers")
public class CustomerEntity {
	@Id
	private String id;
	@Column(nullable = false)
	private String name;
	@Column(name = "company_name")
	private String companyName;
	@Enumerated(EnumType.STRING)
	@Column(name = "customer_type", nullable = false)
	private Enums.CustomerType customerType;
	@Column(nullable = false)
	private String phone;
	@Column(name = "alternate_phone")
	private String alternatePhone;
	private String email;
	private String address;
	private String city;
	private String state;
	private String pincode;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public CustomerEntity() {
	}

	public CustomerEntity(String id, String name, String companyName, Enums.CustomerType customerType, String phone,
			String alternatePhone, String email, String address, String city, String state, String pincode,
			Instant createdAt) {
		this.id = id;
		this.name = name;
		this.companyName = companyName;
		this.customerType = customerType;
		this.phone = phone;
		this.alternatePhone = alternatePhone;
		this.email = email;
		this.address = address;
		this.city = city;
		this.state = state;
		this.pincode = pincode;
		this.createdAt = createdAt;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCompanyName() {
		return companyName;
	}

	public Enums.CustomerType getCustomerType() {
		return customerType;
	}

	public String getPhone() {
		return phone;
	}

	public String getAlternatePhone() {
		return alternatePhone;
	}

	public String getEmail() {
		return email;
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

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void update(String name, String companyName, Enums.CustomerType type, String phone, String alternatePhone,
			String email, String address, String city, String state, String pincode) {
		this.name = name;
		this.companyName = companyName;
		this.customerType = type;
		this.phone = phone;
		this.alternatePhone = alternatePhone;
		this.email = email;
		this.address = address;
		this.city = city;
		this.state = state;
		this.pincode = pincode;
	}
}
