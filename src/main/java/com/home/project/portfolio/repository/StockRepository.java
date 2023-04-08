package com.home.project.portfolio.repository;

import com.home.project.portfolio.model.entity.StockMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<StockMetadata, Long> {

    StockMetadata getByFigi(String figi);

    boolean existsByTicker(String ticker);
}
