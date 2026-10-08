package com.ramkrishna.sales.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "app_users")
public class UserEntity {
	@Id
	private String id;
	@Column(nullable = false, unique = true, length = 100)
	private String username;
	@Column(name = "password_hash", nullable = false)
	private String passwordHash;
	@Column(name = "display_name", nullable = false)
	private String displayName;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Enums.Role role;
	@Column(nullable = false)
	private boolean active = true;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public UserEntity() {
	}

	public UserEntity(String id, String username, String passwordHash, String displayName, Enums.Role role,
			boolean active, Instant createdAt) {
		this.id = id;
		this.username = username;
		this.passwordHash = passwordHash;
		this.displayName = displayName;
		this.role = role;
		this.active = active;
		this.createdAt = createdAt;
	}

	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Enums.Role getRole() {
		return role;
	}

	public boolean isActive() {
		return active;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void update(String username, String displayName, Enums.Role role, boolean active, String passwordHash) {
		this.username = username;
		this.displayName = displayName;
		this.role = role;
		this.active = active;
		if (passwordHash != null && !passwordHash.isBlank())
			this.passwordHash = passwordHash;
	}
}
