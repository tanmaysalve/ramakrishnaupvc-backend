package com.ramkrishna.sales.service;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.entity.*;
import com.ramkrishna.sales.exception.Exceptions;
import com.ramkrishna.sales.repository.*;
import com.ramkrishna.sales.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class DocumentService {
	private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
	private final DocumentRepository docs;
	private final RequestService requests;
	private final UserRepository users;
	private final ActivityService activity;
	private final MapperService mapper;
	private final DocumentStorage storage;

	public DocumentService(DocumentRepository d, RequestService r, UserRepository u, ActivityService a, MapperService m,
			DocumentStorage storage) {
		docs = d;
		requests = r;
		users = u;
		activity = a;
		mapper = m;
		this.storage = storage;
	}

	@Transactional(readOnly = true)
	public List<DocumentDto> list(String requestId, AuthenticatedUser u) {
		requests.visible(requestId, u);
		List<DocumentDto> result = docs.findByRequestIdOrderByUploadedAtDesc(requestId).stream()
				.map(mapper::document).toList();
		log.debug("Listed documents requestId={} userId={} count={}", requestId, u.id(), result.size());
		return result;
	}

	@Transactional
	public DocumentDto upload(String requestId, MultipartFile file, AuthenticatedUser u) {
		RequestEntity r = requests.visible(requestId, u);
		if (file == null || file.isEmpty())
			throw new Exceptions.BadRequest("A non-empty file is required");
		String id = UUID.randomUUID().toString();
		String original = Optional.ofNullable(file.getOriginalFilename()).orElse("file").replace("..", "_")
				.replace("/", "_").replace("\\", "_");
		String stored = id + "_" + original;
		LocalDate requestDate = r.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
		String requestFolder = r.getRequestNumber().replaceAll("[^A-Za-z0-9._-]", "_");
		String key = String.join("/", String.format(Locale.ROOT, "%04d", requestDate.getYear()),
				String.format(Locale.ROOT, "%02d", requestDate.getMonthValue()),
				String.format(Locale.ROOT, "%02d", requestDate.getDayOfMonth()), requestFolder, stored);
		try {
			storage.store(key, file);
		} catch (IOException e) {
			log.error("Document upload failed requestId={} userId={} documentId={}", requestId, u.id(), id, e);
			throw new Exceptions.BadRequest("Unable to store file");
		}
		UserEntity user = users.findById(u.id()).orElseThrow();
		DocumentEntity d = new DocumentEntity(id, r, original, stored, file.getContentType(), file.getSize(), key, user,
				Instant.now());
		docs.save(d);
		activity.add(r, u.id(), Enums.ActivityType.DOCUMENT_UPLOADED, "uploaded " + original, null, null, id);
		log.info("Document uploaded documentId={} requestId={} userId={} sizeBytes={}", id, requestId, u.id(),
				file.getSize());
		return mapper.document(d);
	}

	public Resource resource(String id, AuthenticatedUser u) {
		DocumentEntity d = docs.findById(id).orElseThrow(() -> new Exceptions.NotFound("Document not found"));
		requests.visible(d.getRequest().getId(), u);
		try {
			Resource r = storage.load(d.getStorageKey());
			log.info("Document download authorized documentId={} requestId={} userId={}", id,
					d.getRequest().getId(), u.id());
			return r;
		} catch (IOException e) {
			log.error("Document download failed documentId={} userId={}", id, u.id(), e);
			throw new Exceptions.NotFound("Stored file not found");
		} catch (IllegalArgumentException e) {
			log.error("Document download failed documentId={} userId={}", id, u.id(), e);
			throw new Exceptions.NotFound("Stored file not found");
		}
	}

	@Transactional
	public void delete(String id, AuthenticatedUser u) {
		DocumentEntity d = docs.findById(id).orElseThrow(() -> new Exceptions.NotFound("Document not found"));
		RequestEntity r = requests.visible(d.getRequest().getId(), u);
		try {
			storage.delete(d.getStorageKey());
		} catch (IOException | IllegalArgumentException e) {
			log.error("Could not delete document file documentId={} requestId={}", id, r.getId(), e);
		}
		docs.delete(d);
		activity.add(r, u.id(), Enums.ActivityType.DOCUMENT_REMOVED, "removed document " + d.getFileName(), null, null,
				d.getId());
		log.info("Document deleted documentId={} requestId={} userId={}", id, r.getId(), u.id());
	}

	public DocumentEntity entity(String id) {
		return docs.findById(id).orElseThrow(() -> new Exceptions.NotFound("Document not found"));
	}
}
