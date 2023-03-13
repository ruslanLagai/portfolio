package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.helpers.TestUtils;
import com.home.project.portfolio.model.analytic.Period;
import com.home.project.portfolio.model.entity.OperationEntity;
import com.home.project.portfolio.model.operations.Instrument;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.Operations;
import com.home.project.portfolio.repository.OperationRepository;
import com.home.project.portfolio.utils.ConversionUtils;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.annotation.PostConstruct;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.home.project.portfolio.utils.Profiles.CUSTOM_DB_TEST_PROFILE;
import static com.home.project.portfolio.utils.Profiles.TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Class to test {@link OperationsService}
 */
@Disabled("Temporarily disabled due to refactoring")
@ActiveProfiles(value = {CUSTOM_DB_TEST_PROFILE, TEST_PROFILE})
@Testcontainers
@ExtendWith(SpringExtension.class)
@DisplayName("Test retrieving operations")
@ContextConfiguration(classes = AbstractDbTest.Config.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
class OperationsServiceTest extends AbstractDbTest {

    public static final String ACCOUNT_ID = "2000686010";
    @Autowired
    private OperationsService operationsService;

    @Autowired
    private OperationRepository operationRepository;

    @MockBean
    private TinkoffClient tinkoffClient;

    private static Set<OperationEntity> dbOperations = new HashSet<>();
    private static Operations restOperations;
    private static Operation latestOperation;
    private static Operation latestDbOperation;

    @BeforeAll
    public static void setUp() {
        var response = TestUtils.readOperations("src/test/resources/testData/brokerAccountOperations.json");
        var operations = response
                .getPayload().getOperations().stream()
                .sorted(Comparator.comparing(Operation::getDate, Comparator.reverseOrder()))
                .collect(Collectors.toList());
        latestOperation = operations.get(0);
        latestDbOperation = operations.get(10);
        response.getPayload().setOperations(response.getPayload().getOperations().subList(0, 10));
        restOperations = response;
        operations.removeAll(operations.subList(0, 10));
        dbOperations = ConversionUtils.convertToDbOperations(operations, ACCOUNT_ID);
    }

    @PostConstruct
    public void initData() {
        operationRepository.saveAll(dbOperations);
    }


    @Test
    @DisplayName("Test no new operations from rest")
    void a1getLastOperations() {
        var payload = new Operations.Payload();
        payload.setOperations(Collections.emptyList());
        var toReturn = new Operations();
        toReturn.setStatus("ok");
        toReturn.setPayload(payload);
        when(tinkoffClient.getOperations(any(), any(), eq(ACCOUNT_ID))).thenReturn(toReturn);

        var result = operationsService.getLastOperations(ACCOUNT_ID, Period.LAST_SIX_MONTH);
        var savedOperations = operationRepository.getByAccountId(ACCOUNT_ID);
        assertAll(() -> {
            assertThat(result.size(), Matchers.greaterThan(10));
            assertThat(result.get(0).getId(), Matchers.equalTo(latestDbOperation.getId()));
            assertThat(savedOperations.size(), Matchers.equalTo(dbOperations.size()));
        });
    }

    @Test
    @DisplayName("Test operations in DB")
    void a2getLastOperations() {
        when(tinkoffClient.getOperations(any(), any(), eq(ACCOUNT_ID))).thenReturn(restOperations);
        when(tinkoffClient.getInstrumentInfoByFigi(any())).thenReturn(new Instrument());

        var result = operationsService.getLastOperations(ACCOUNT_ID, Period.LAST_SIX_MONTH);
        var savedOperations = operationRepository.getByAccountId(ACCOUNT_ID);
        assertAll(() -> {
            assertThat(result.size(), Matchers.greaterThan(10));
            assertThat(result.get(0), Matchers.equalTo(latestOperation));
            assertThat(savedOperations.size(), Matchers.equalTo(dbOperations.size() + 10));
        });
    }

    @Test
    @DisplayName("Test no operations in DB")
    void a3getLastOperations() {
        when(tinkoffClient.getOperations(any(), any(), eq(ACCOUNT_ID))).thenReturn(restOperations);
        when(tinkoffClient.getInstrumentInfoByFigi(any())).thenReturn(new Instrument());

        operationRepository.deleteAll();

        var result = operationsService.getLastOperations(ACCOUNT_ID, Period.LAST_SIX_MONTH);
        var savedOperations = operationRepository.getByAccountId(ACCOUNT_ID);
        assertAll(() -> {
            assertThat(result.size(), Matchers.equalTo(9));
            assertThat(result.get(0), Matchers.equalTo(latestOperation));
            assertThat(savedOperations.size(), Matchers.equalTo(10));
        });
    }

    @Test
    @DisplayName("Test no older operations")
    void b1getLastOperationsForStock() {
        var payload = new Operations.Payload();
        payload.setOperations(Collections.emptyList());
        var toReturn = new Operations();
        toReturn.setStatus("ok");
        toReturn.setPayload(payload);
        when(tinkoffClient.getOperationsOnStock(any(), any(), any(), eq(ACCOUNT_ID))).thenReturn(toReturn);

        var result = operationsService.getLastOperationsForStock(ZonedDateTime.now(), ZonedDateTime.now(), "figi", ACCOUNT_ID);
        assertThat(result.size(), Matchers.equalTo(0));

        when(tinkoffClient.getOperationsOnStock(any(), any(), any(), eq(ACCOUNT_ID)))
                .thenThrow(new FeignException.TooManyRequests("",
                        Request.create(Request.HttpMethod.GET, "url", Map.of(), null, new RequestTemplate()), null,
                    Map.of()));
        result = operationsService.getLastOperationsForStock(ZonedDateTime.now(), ZonedDateTime.now(), "figi", ACCOUNT_ID);
        assertThat(result.size(), Matchers.equalTo(0));

        when(tinkoffClient.getOperationsOnStock(any(), any(), any(), eq(ACCOUNT_ID))).thenReturn(restOperations);
        result = operationsService.getLastOperationsForStock(ZonedDateTime.now(), ZonedDateTime.now(), "figi", ACCOUNT_ID);
        assertThat(result.size(), Matchers.equalTo(10));
    }


}