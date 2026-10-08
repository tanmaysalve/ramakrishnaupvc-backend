package com.ramkrishna.sales.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "request_activities")
public class ActivityEntity {
	@Id
	private String id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "request_id")
	private RequestEntity request;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private UserEntity user;
	@Enumerated(EnumType.STRING)
	@Column(name = "activity_type", nullable = false)
	private Enums.ActivityType type;
	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;
	@Column(name = "old_value")
	private String oldValue;
	@Column(name = "new_value")
	private String newValue;
	@Column(name = "document_id")
	private String documentId;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public ActivityEntity() {
	}

	public ActivityEntity(String id, RequestEntity request, UserEntity user, Enums.ActivityType type,
			String description, String oldValue, String newValue, String documentId, Instant createdAt) {
		this.id = id;
		this.request = request;
		this.user = user;
		this.type = type;
		this.description = description;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.documentId = documentId;
		this.createdAt = createdAt;
	}

	public String getId() {
		return id;
	}

	public RequestEntity getRequest() {
		return request;
	}

	public UserEntity getUser() {
		return user;
	}

	public Enums.ActivityType getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public String getOldValue() {
		return oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public String getDocumentId() {
		return documentId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
