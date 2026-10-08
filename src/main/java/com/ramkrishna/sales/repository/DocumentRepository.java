package com.ramkrishna.sales.repository;
import com.ramkrishna.sales.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;

public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {
	List<DocumentEntity> findByRequestIdOrderByUploadedAtDesc(String requestId); }
