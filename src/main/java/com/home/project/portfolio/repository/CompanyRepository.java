package com.home.project.portfolio.repository;

import com.home.project.portfolio.model.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author rlagay
 */
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    Optional<CompanyEntity> findByTicker(String ticker);
}
