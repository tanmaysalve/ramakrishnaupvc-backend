package com.ramkrishna.sales.dto;

import com.ramkrishna.sales.entity.Enums.*;
import java.time.*;
import java.util.*;

public final class ApiDtos {
	private ApiDtos() {
	}

	public record UserDto(String id, String username, String displayName, Role role, boolean active,
			Instant createdAt) {
	}

	public record UserRequest(String username, String displayName, Role role, Boolean active, String password) {
	}

	public record LoginRequest(String username, String password) {
	}

	public record LoginResponse(String token, UserDto user) {
	}

	public record CustomerDto(String id, String name, String companyName, CustomerType customerType, String phone,
			String alternatePhone, String email, String address, String city, String state, String pincode,
			Instant createdAt) {
	}

	public record CustomerRequest(String name, String companyName, CustomerType customerType, String phone,
			String alternatePhone, String email, String address, String city, String state, String pincode) {
	}

	public record SiteDto(String id, String customerId, String name, String address, String city, String state,
			String pincode, String contactPerson, String contactPhone, String notes, Instant createdAt) {
	}

	public record ListPageDto<T>(List<T> data, PaginationDto pagination) {
	}

	public record PaginationDto(int page, int limit, long total, int totalPages) {
	}

	public record SiteRequest(String customerId, String name, String address, String city, String state, String pincode,
			String contactPerson, String contactPhone, String notes) {
	}

	public record RequestDto(String id, String requestNumber, String customerId, String siteId, String createdBy,
			String assignedTo, RequestSource source, RequestType requestType, ProductCategory productCategory,
			String description, Integer quantity, Priority priority, RequestStatus status, LocalDate expectedDate,
			String remarks, Instant createdAt, Instant updatedAt) {
	}

	public record RequestPageDto(List<RequestDto> content, int page, int size, long totalElements, int totalPages,
			boolean first, boolean last, boolean hasNext, boolean hasPrevious) {
	}

	public record RequestCreateRequest(String customerId, String siteId, RequestSource source, RequestType requestType,
			ProductCategory productCategory, String description, Integer quantity, Priority priority,
			RequestStatus status, LocalDate expectedDate, String remarks, String assignedTo) {
	}

	public record RequestUpdateRequest(RequestSource source, RequestType requestType, ProductCategory productCategory,
			String description, Integer quantity, Priority priority, LocalDate expectedDate, String remarks) {
	}

	public record StatusRequest(RequestStatus status) {
	}

	public record AssignmentRequest(String assignedTo) {
	}

	public record CommentRequest(String text) {
	}

	public record ActivityDto(String id, String requestId, String userId, String userDisplayName, ActivityType type,
			String description, String oldValue, String newValue, String documentId, Instant createdAt) {
	}

	public record DocumentDto(String id, String requestId, String fileName, String contentType, long size,
			String uploadedBy, String uploadedByName, Instant uploadedAt) {
	}

	public record DashboardDto(long total, long newCount, long contacted, long siteVisit, long requirements,
			long followUp, long quotationPending, long confirmed, long inProgress, long completed, long cancelled,
			Map<String, Long> bySalesUser, Map<String, Long> bySource, List<RequestDto> recentRequests,
			List<ActivityDto> recentActivities, long attentionCount) {
	}

	public record MessageResponse(String message) {
	}
}
