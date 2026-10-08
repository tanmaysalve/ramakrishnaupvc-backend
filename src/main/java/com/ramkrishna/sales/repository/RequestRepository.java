package com.ramkrishna.sales.repository;

import com.ramkrishna.sales.entity.RequestEntity;
import com.ramkrishna.sales.entity.Enums.Priority;
import com.ramkrishna.sales.entity.Enums.RequestSource;
import com.ramkrishna.sales.entity.Enums.RequestStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<RequestEntity, String> {
	List<RequestEntity> findAllByOrderByUpdatedAtDesc();

	List<RequestEntity> findByCreatedByIdOrAssignedToIdOrderByUpdatedAtDesc(String createdBy, String assignedTo);

	@Query("select r from RequestEntity r left join fetch r.assignedTo where r.id = :id")
	Optional<RequestEntity> findByIdWithAssignedTo(@Param("id") String id);

	@EntityGraph(attributePaths = { "customer", "site", "createdBy", "assignedTo" })
	@Query("""
			select r from RequestEntity r
			where (:admin = true or r.createdBy.id = :userId or r.assignedTo.id = :userId)
			and (:search = ''
				or lower(r.requestNumber) like lower(concat('%', :search, '%'))
				or lower(r.description) like lower(concat('%', :search, '%'))
				or lower(r.customer.name) like lower(concat('%', :search, '%'))
				or lower(r.customer.companyName) like lower(concat('%', :search, '%')))
			and (:status is null or r.status = :status)
			and (:priority is null or r.priority = :priority)
			and (:source is null or r.source = :source)
			and (:assignedTo is null or r.assignedTo.id = :assignedTo)
			and (:unassigned = false or r.assignedTo is null)
			""")
	Page<RequestEntity> findVisibleRequests(@Param("admin") boolean admin, @Param("userId") String userId,
			@Param("search") String search, @Param("status") RequestStatus status,
			@Param("priority") Priority priority, @Param("source") RequestSource source,
			@Param("assignedTo") String assignedTo, @Param("unassigned") boolean unassigned, Pageable pageable);

	@Query(value = "select nextval('request_number_seq')", nativeQuery = true)
	long nextRequestSequence();
}
