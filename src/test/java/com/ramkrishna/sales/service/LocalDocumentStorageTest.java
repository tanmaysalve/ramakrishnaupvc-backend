package com.ramkrishna.sales.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class LocalDocumentStorageTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void storesDocumentsUnderTheRelativeKey() throws Exception {
		LocalDocumentStorage storage = new LocalDocumentStorage(temporaryDirectory.toString());
		String key = "2026/10/06/REQ-123/document.pdf";
		MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf",
				"document contents".getBytes());

		storage.store(key, file);

		assertEquals("document contents", Files.readString(temporaryDirectory.resolve(key), StandardCharsets.UTF_8));
		assertEquals("document contents",
				new String(storage.load(key).getInputStream().readAllBytes(), StandardCharsets.UTF_8));
	}

	@Test
	void rejectsKeysThatEscapeTheStorageRoot() {
		LocalDocumentStorage storage = new LocalDocumentStorage(temporaryDirectory.toString());

		assertThrows(IllegalArgumentException.class,
				() -> storage.store("../outside.pdf", new MockMultipartFile("file", new byte[0])));
	}

	@Test
	void remapsExistingAbsolutePathsAfterTheStorageRootChanges() throws Exception {
		LocalDocumentStorage storage = new LocalDocumentStorage(temporaryDirectory.toString());
		Path legacyFile = temporaryDirectory.resolve("old-root/2026/10/06/REQ-123/document.pdf");
		Path migratedFile = temporaryDirectory.resolve("2026/10/06/REQ-123/document.pdf");
		Files.createDirectories(migratedFile.getParent());
		Files.writeString(migratedFile, "legacy document");

		assertEquals("legacy document", new String(storage.load(legacyFile.toString()).getInputStream().readAllBytes(),
				StandardCharsets.UTF_8));
	}
}
