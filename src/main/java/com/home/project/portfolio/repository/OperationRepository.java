package com.home.project.portfolio.repository;

import com.home.project.portfolio.model.entity.OperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface OperationRepository extends JpaRepository<OperationEntity, Long> {

    List<OperationEntity> getByAccountIdAndDateBetweenOrderByDateDesc(String accountId, ZonedDateTime start,
                                                       ZonedDateTime end);

    List<OperationEntity> getByAccountId(String accountId);
}
