package com.ramkrishna.sales.controller;

import com.ramkrishna.sales.dto.ApiDtos.*;
import com.ramkrishna.sales.security.AuthenticatedUser;
import com.ramkrishna.sales.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
	private final DocumentService service;

	public DocumentController(DocumentService s) {
		service = s;
	}

	@PostMapping(value = "/request/{requestId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DocumentDto upload(@PathVariable String requestId, @RequestPart("file") MultipartFile file,
			@AuthenticationPrincipal AuthenticatedUser u) {
		return service.upload(requestId, file, u);
	}

	@GetMapping("/{id}/download")
	public ResponseEntity<Resource> download(@PathVariable String id, @AuthenticationPrincipal AuthenticatedUser u) {
		var d = service.entity(id);
		Resource r = service.resource(id, u);
		MediaType mt;
		try {
			mt = d.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM
					: MediaType.parseMediaType(d.getContentType());
		} catch (Exception e) {
			mt = MediaType.APPLICATION_OCTET_STREAM;
		}
		return ResponseEntity.ok().contentType(mt).header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + d.getFileName().replace("\"", "_") + "\"").body(r);
	}

	@DeleteMapping("/{id}")
	public MessageResponse delete(@PathVariable String id, @AuthenticationPrincipal AuthenticatedUser u) {
		service.delete(id, u);
		return new MessageResponse("Document deleted");
	}
}
