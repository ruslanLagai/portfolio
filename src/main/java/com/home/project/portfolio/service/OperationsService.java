package com.home.project.portfolio.service;

import com.home.project.portfolio.mapper.OperationMapper;
import com.home.project.portfolio.model.analytic.Period;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.Status;
import com.home.project.portfolio.repository.OperationRepository;
import com.home.project.portfolio.utils.ExecutorServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.tinkoff.piapi.core.exception.ApiRuntimeException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Service to retrieve operations
 * Part operations are retrieved from DB, latest from Tinkoff
 */
@Service
@Slf4j
public record OperationsService(ru.tinkoff.piapi.core.OperationsService operationsService,
                                OperationRepository operationRepository,
                                StockHelperService stockHelperService,
                                OperationMapper operationMapper) {

    public List<Operation> getLastOperations(String accountId, LocalDate date) {
        log.info("Retrieving operations for account {}, from {}", accountId, date);

        List<Operation> operations = new ArrayList<>();
        ZonedDateTime lastDbOperation = null;
        var start = ZonedDateTime.of(date.atStartOfDay(), ZoneId.systemDefault());
        var end = ZonedDateTime.now();
        var dbOperations = operationRepository
            .getByAccountIdAndDateBetweenOrderByDateDesc(accountId, start, end);

        // find the last operation to use it with Tinkoff API
        if (!CollectionUtils.isEmpty(dbOperations)) {
            lastDbOperation = dbOperations.iterator().next().getDate();
            log.debug("Latest operation from DB has time {}", end);
        }

        try {
            // query Tinkoff API, it treats start date as inclusive, i.e. [start ... end)
            var newOperations = operationsService.getExecutedOperationsSync(
                accountId,
                lastDbOperation != null ? Instant.from(lastDbOperation) : Instant.from(start),
                Instant.from(end));

            // if the response contains some data, it may contain entries already stored in the database
            log.debug("Retrieved new {} operations", newOperations.size());

            // filter out the ones which we have in the DB already
            newOperations.stream()
                .filter(operation ->
                    dbOperations.stream().
                        filter(o -> Objects.equals(o.getOperationId(), operation.getId())).
                        findFirst()
                        .isEmpty())
                .collect(Collectors.toSet())
                .forEach(operation -> {
                    var ticker = stockHelperService.findTicker(operation);
                    var mappedOperation = operationMapper.map(operation, ticker);
                    operations.add(mappedOperation);
                });

            // store true new operations to the database
            ExecutorServiceUtils.execute(() -> {
                var entities = operationMapper.mapToEntities(operations, accountId);
                operationRepository.saveAll(entities);
            }, Executors.newSingleThreadExecutor());
        } catch (ApiRuntimeException e) {
            log.error("Failed to retrieve latest operations for account {}, status code {}, \nException: {}",
                accountId, e.getCode(), e.getMessage());
        }

        log.info("Retrieved {} operations from: {}. Starting from {}", operations.size(), date, start);

        // add the old list of operations from database operations for the period selected
        operations.addAll(operationMapper.mapToRest(dbOperations));

        return operations.stream()
                .filter(operation -> operation.getStatus().equals(Status.DONE))
                .sorted(Comparator.comparing(Operation::getDate, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    public List<Operation> getLastOperationsForStock(ZonedDateTime from, ZonedDateTime to, String figi, String accountId) {
        try {
            return operationsService
                    .getExecutedOperationsSync(accountId, Instant.from(from), Instant.from(to), figi)
                .stream()
                .map(operation -> {
                    var ticker = stockHelperService.findTicker(operation);
                    return operationMapper.map(operation, ticker);
                })
                .collect(Collectors.toList());
        } catch (ApiRuntimeException e) {
            log.error("Failed to retrieve latest operations for account {}, status code {}, \nException: {}",
                accountId, e.getCode(), e.getMessage(), e);
            return List.of();
        }
    }
}
