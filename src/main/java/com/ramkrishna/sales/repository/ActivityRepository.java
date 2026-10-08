package com.ramkrishna.sales.repository;
import com.ramkrishna.sales.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ActivityRepository extends JpaRepository<ActivityEntity,String> { List<ActivityEntity> findByRequestIdOrderByCreatedAtDesc(String requestId); }
