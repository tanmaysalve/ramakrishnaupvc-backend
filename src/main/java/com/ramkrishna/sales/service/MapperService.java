package com.ramkrishna.sales.service;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.entity.*;
import org.springframework.stereotype.Service;

@Service
public class MapperService {
	public UserDto user(UserEntity u) {
		return new UserDto(u.getId(), u.getUsername(), u.getDisplayName(), u.getRole(), u.isActive(), u.getCreatedAt());
	}

	public CustomerDto customer(CustomerEntity c) {
		return new CustomerDto(c.getId(), c.getName(), c.getCompanyName(), c.getCustomerType(), c.getPhone(),
				c.getAlternatePhone(), c.getEmail(), c.getAddress(), c.getCity(), c.getState(), c.getPincode(),
				c.getCreatedAt());
	}

	public SiteDto site(SiteEntity s) {
		return new SiteDto(s.getId(), s.getCustomer().getId(), s.getName(), s.getAddress(), s.getCity(), s.getState(),
				s.getPincode(), s.getContactPerson(), s.getContactPhone(), s.getNotes(), s.getCreatedAt());
	}

	public RequestDto request(RequestEntity r) {
		return new RequestDto(r.getId(), r.getRequestNumber(), r.getCustomer().getId(),
				r.getSite() == null ? null : r.getSite().getId(), r.getCreatedBy().getId(),
				r.getAssignedTo() == null ? null : r.getAssignedTo().getId(), r.getSource(), r.getRequestType(),
				r.getProductCategory(), r.getDescription(), r.getQuantity(), r.getPriority(), r.getStatus(),
				r.getExpectedDate(), r.getRemarks(), r.getCreatedAt(), r.getUpdatedAt());
	}

	public ActivityDto activity(ActivityEntity a) {
		return new ActivityDto(a.getId(), a.getRequest().getId(), a.getUser().getId(), a.getUser().getDisplayName(),
				a.getType(), a.getDescription(), a.getOldValue(), a.getNewValue(), a.getDocumentId(), a.getCreatedAt());
	}

	public DocumentDto document(DocumentEntity d) {
		return new DocumentDto(d.getId(), d.getRequest().getId(), d.getFileName(), d.getContentType(), d.getSize(),
				d.getUploadedBy().getId(), d.getUploadedBy().getDisplayName(), d.getUploadedAt());
	}
}
