package com.ramkrishna.sales.service;

import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorage {
	void store(String key, MultipartFile file) throws IOException;

	Resource load(String key) throws IOException;

	void delete(String key) throws IOException;
}
