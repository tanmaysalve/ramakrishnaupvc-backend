package com.ramkrishna.sales.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalDocumentStorage implements DocumentStorage {
	private final Path root;

	public LocalDocumentStorage(@Value("${app.storage.documents}") String storage) {
		root = Paths.get(storage).toAbsolutePath().normalize();
		try {
			Files.createDirectories(root);
		} catch (IOException e) {
			throw new IllegalStateException("Cannot create document storage directory", e);
		}
	}

	@Override
	public void store(String key, MultipartFile file) throws IOException {
		Path target = resolveRelativeKey(key);
		Files.createDirectories(target.getParent());
		file.transferTo(target);
	}

	@Override
	public Resource load(String key) throws IOException {
		Path path = resolveKey(key);
		Resource resource = new UrlResource(path.toUri());
		if (!resource.exists() || !resource.isReadable())
			throw new IOException("Stored file is missing or unreadable");
		return resource;
	}

	@Override
	public void delete(String key) throws IOException {
		Files.deleteIfExists(resolveKey(key));
	}

	private Path resolveKey(String key) {
		Path storedPath = Paths.get(key).normalize();
		if (storedPath.isAbsolute()) {
			if (storedPath.getNameCount() < 5)
				throw new IllegalArgumentException("Legacy document path does not contain a valid storage key");
			Path legacyKey = storedPath.subpath(storedPath.getNameCount() - 5, storedPath.getNameCount());
			return resolveRelativeKey(legacyKey.toString());
		}
		return resolveRelativeKey(key);
	}

	private Path resolveRelativeKey(String key) {
		Path relativePath = Paths.get(key);
		if (key.isBlank() || relativePath.isAbsolute())
			throw new IllegalArgumentException("Document storage key must be relative");
		Path target = root.resolve(relativePath).normalize();
		if (!target.startsWith(root))
			throw new IllegalArgumentException("Document storage key escapes the configured storage root");
		return target;
	}
}
