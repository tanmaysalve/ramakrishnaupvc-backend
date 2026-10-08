package com.ramkrishna.sales.repository;
import com.ramkrishna.sales.entity.SiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SiteRepository extends JpaRepository<SiteEntity, String>, JpaSpecificationExecutor<SiteEntity> {
}
