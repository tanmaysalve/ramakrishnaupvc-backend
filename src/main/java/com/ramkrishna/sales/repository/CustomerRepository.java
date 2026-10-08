package com.ramkrishna.sales.repository;
import com.ramkrishna.sales.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerRepository
		extends JpaRepository<CustomerEntity, String>, JpaSpecificationExecutor<CustomerEntity> {
}
