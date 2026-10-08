package com.ramkrishna.sales.repository;

import com.ramkrishna.sales.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserRepository extends JpaRepository<UserEntity, String> {
	Optional<UserEntity> findByUsername(String username);

	List<UserEntity> findAllByOrderByDisplayNameAsc();
}
