package com.home.project.portfolio.service;

import com.home.project.portfolio.helpers.TestUtils;
import com.home.project.portfolio.mapper.OperationMapper;
import com.home.project.portfolio.model.InstrumentType;
import com.home.project.portfolio.model.entity.OperationEntity;
import com.home.project.portfolio.model.operations.Status;
import com.home.project.portfolio.repository.OperationRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.InstrumentsService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BROKER_FEE;

/**
 * Class to test {@link OperationsService}
 */
@Testcontainers
@DisplayName("Test retrieving operations")
@SpringBootTest
@ContextConfiguration(initializers = AbstractDbTest.Initializer.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
class OperationsServiceTest extends AbstractDbTest {

    public static final String ACCOUNT_ID = "2000686010";

    @Autowired
    private OperationsService operationsService;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private OperationMapper operationMapper;

    @MockBean
    private ru.tinkoff.piapi.core.OperationsService tinkoffOperationService;

    @MockBean
    private InstrumentsService instrumentsService;

    private List<OperationEntity> dbOperations;
    private List<Operation> restOperations;
    private Operation latestOperation;
    private Operation latestDbOperation;

    @PostConstruct
    public void initData() {
        var operations = TestUtils.operations("classpath:testData/get-operations.json");
        latestOperation = operations.get(0);
        latestDbOperation = operations.get(10);
        restOperations = operations.subList(0, 10);

        var convertedOperations = operations.stream()
            .filter(operation -> !restOperations.contains(operation))
            .map(operation -> operationMapper.map(operation, ACCOUNT_ID))
            .toList();
        dbOperations = operationMapper.mapToEntities(convertedOperations, ACCOUNT_ID);
        if (operationRepository.findAll().isEmpty()) {
            operationRepository.saveAll(dbOperations);
        }
    }

    @PreDestroy
    public void clear() {
        operationRepository.deleteAll();
    }

    @Test
    @DisplayName("Test no new operations from rest")
    void a1getLastOperations() {
        when(tinkoffOperationService.getExecutedOperationsSync(eq(ACCOUNT_ID), any(), any())).thenReturn(List.of());

        var result = operationsService.getLastOperations(ACCOUNT_ID, LocalDate.now().minus(java.time.Period.ofMonths(6)));
        var savedOperations = operationRepository.getByAccountId(ACCOUNT_ID);
        assertAll(() -> {
            assertThat(result.size(), Matchers.greaterThan(10));
            assertThat(result.get(0).getId(), Matchers.equalTo(latestDbOperation.getId()));
            assertThat(savedOperations.size(), Matchers.equalTo(dbOperations.size()));
        });
    }

    @Test
    @DisplayName("Test new operations from tinkoff")
    void a2getLastOperations() {
        when(tinkoffOperationService.getExecutedOperationsSync(eq(ACCOUNT_ID), any(), any())).thenReturn(restOperations);
        when(instrumentsService.getInstrumentByFigiSync(any()))
            .thenReturn(ru.tinkoff.piapi.contract.v1.Instrument.newBuilder().setTicker("ticker").build());

        var result = operationsService.getLastOperations(ACCOUNT_ID, LocalDate.now().minus(java.time.Period.ofMonths(6)));
        var savedOperations = operationRepository.getByAccountId(ACCOUNT_ID);
        assertAll(() -> {
            assertThat(result.size(), Matchers.greaterThan(10));
            assertThat(result.get(0), Matchers.equalTo(operationMapper.map(latestOperation, "ticker")));
            assertThat(savedOperations.size(), Matchers.equalTo(dbOperations.size() + 10));
        });
    }

    @Test
    @DisplayName("Test no operations in DB")
    void a3getLastOperations() {
        when(tinkoffOperationService.getExecutedOperationsSync(eq(ACCOUNT_ID), any(), any())).thenReturn(restOperations);
        when(instrumentsService.getInstrumentByFigiSync(any()))
            .thenReturn(ru.tinkoff.piapi.contract.v1.Instrument.newBuilder().setTicker("ticker").build());

        operationRepository.deleteAll();

        var result = operationsService.getLastOperations(ACCOUNT_ID, LocalDate.now().minus(java.time.Period.ofMonths(6)));
        var savedOperations = operationRepository.getByAccountId(ACCOUNT_ID);
        assertAll(() -> {
            assertThat(result.size(), Matchers.equalTo(10));
            assertThat(result.get(0), Matchers.equalTo(operationMapper.map(latestOperation, "ticker")));
            assertThat(savedOperations.size(), Matchers.equalTo(10));
        });
    }

    @Test
    @DisplayName("Test get operations on stock")
    void b1getLastOperationsForStock() {
        when(tinkoffOperationService.getExecutedOperationsSync(eq(ACCOUNT_ID), any(), any())).thenReturn(List.of());
        var result = operationsService.getLastOperationsForStock(ZonedDateTime.now(), ZonedDateTime.now(), "figi", ACCOUNT_ID);
        assertThat(result.size(), Matchers.equalTo(0));

        when(tinkoffOperationService.getExecutedOperationsSync(eq(ACCOUNT_ID), any(), any(), eq("figi")))
            .thenReturn(restOperations);
        when(instrumentsService.getInstrumentByFigiSync(any()))
            .thenReturn(ru.tinkoff.piapi.contract.v1.Instrument.newBuilder().setTicker("ticker").build());

        result = operationsService.getLastOperationsForStock(ZonedDateTime.now(), ZonedDateTime.now(), "figi", ACCOUNT_ID);

        assertEquals(10, result.size());
        assertEquals("BBG000BLKK03", result.get(0).getFigi());
        assertEquals("ticker", result.get(0).getTicker());
        assertEquals(InstrumentType.STOCK, result.get(0).getInstrumentType());
        assertEquals(OPERATION_TYPE_BROKER_FEE, result.get(0).getOperationType());
        assertEquals(0, result.get(0).getQuantity());
        assertEquals(0, result.get(0).getQuantityExecuted());
        assertEquals(Status.DONE, result.get(0).getStatus());
    }
}