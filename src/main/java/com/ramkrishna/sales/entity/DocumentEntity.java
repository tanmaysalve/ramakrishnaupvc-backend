package com.ramkrishna.sales.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "documents")
public class DocumentEntity {
	@Id
	private String id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "request_id")
	private RequestEntity request;
	@Column(name = "file_name", nullable = false)
	private String fileName;
	@Column(name = "stored_file_name", nullable = false)
	private String storedFileName;
	@Column(name = "content_type")
	private String contentType;
	@Column(name = "size_bytes", nullable = false)
	private long size;
	@Column(name = "storage_key", nullable = false)
	private String storageKey;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "uploaded_by")
	private UserEntity uploadedBy;
	@Column(name = "uploaded_at", nullable = false)
	private Instant uploadedAt;

	public DocumentEntity() {
	}

	public DocumentEntity(String id, RequestEntity request, String fileName, String storedFileName, String contentType,
			long size, String storageKey, UserEntity uploadedBy, Instant uploadedAt) {
		this.id = id;
		this.request = request;
		this.fileName = fileName;
		this.storedFileName = storedFileName;
		this.contentType = contentType;
		this.size = size;
		this.storageKey = storageKey;
		this.uploadedBy = uploadedBy;
		this.uploadedAt = uploadedAt;
	}

	public String getId() {
		return id;
	}

	public RequestEntity getRequest() {
		return request;
	}

	public String getFileName() {
		return fileName;
	}

	public String getStoredFileName() {
		return storedFileName;
	}

	public String getContentType() {
		return contentType;
	}

	public long getSize() {
		return size;
	}

	public String getStorageKey() {
		return storageKey;
	}

	public UserEntity getUploadedBy() {
		return uploadedBy;
	}

	public Instant getUploadedAt() {
		return uploadedAt;
	}
}
