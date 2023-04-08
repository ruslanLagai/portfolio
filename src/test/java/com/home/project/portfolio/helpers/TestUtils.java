package com.home.project.portfolio.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.protobuf.util.JsonFormat;
import com.home.project.portfolio.model.operations.Operations;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;
import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.contract.v1.GetLastPricesResponse;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationsResponse;
import ru.tinkoff.piapi.contract.v1.PortfolioResponse;
import ru.tinkoff.piapi.contract.v1.PositionsResponse;
import ru.tinkoff.piapi.core.models.Positions;

import java.io.File;
import java.util.List;


public class TestUtils {

    private final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        mapper.registerModule(new JavaTimeModule());
    }

    public static <T> T readValue(String resourcePath, Class<T> clazz) throws Exception {
        return mapper.readValue(new File(resourcePath), clazz);
    }

    @SneakyThrows
    public static Operations readOperations() {
        String resourcePath = ResourceUtils.getFile("src/test/resources/testData/mockOperations.json").getAbsolutePath();

        try {
            return readValue(resourcePath, Operations.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read operations file");
        }
    }

    @SneakyThrows
    public static <T> T  getResource(String file, TypeReference<T> type) {
        var resource = ResourceUtils.getFile(file).getAbsolutePath();
        return mapper.readValue(new File(resource), type);
    }

    @SneakyThrows
    public static Account account(String path) {
        Account.Builder builder = Account.newBuilder();
        var content = FileUtils.readFileToString(new File(ResourceUtils.getFile(path).getAbsolutePath()));
        JsonFormat.parser().merge(content, builder);
        return builder.build();
    }

    @SneakyThrows
    public static Positions positions(String path) {
        PositionsResponse.Builder builder = PositionsResponse.newBuilder();
        var content = FileUtils.readFileToString(new File(ResourceUtils.getFile(path).getAbsolutePath()));
        JsonFormat.parser().merge(content, builder);
        return Positions.fromResponse(builder.build());
    }

    @SneakyThrows
    public static ru.tinkoff.piapi.core.models.Portfolio portfolio(String path) {
        PortfolioResponse.Builder builder = PortfolioResponse.newBuilder();
        var content = FileUtils.readFileToString(new File(ResourceUtils.getFile(path).getAbsolutePath()));
        JsonFormat.parser().merge(content, builder);
        return ru.tinkoff.piapi.core.models.Portfolio.fromResponse(builder.build());
    }

    @SneakyThrows
    public static List<LastPrice> lastPrices(String path) {
        GetLastPricesResponse.Builder builder = GetLastPricesResponse.newBuilder();
        var content = FileUtils.readFileToString(new File(ResourceUtils.getFile(path).getAbsolutePath()));
        JsonFormat.parser().merge(content, builder);
        return builder.build().getLastPricesList();
    }

    @SneakyThrows
    public static Operation operation(String path) {
        Operation.Builder builder = Operation.newBuilder();
        var content = FileUtils.readFileToString(new File(ResourceUtils.getFile(path).getAbsolutePath()));
        JsonFormat.parser().merge(content, builder);
        return builder.build();
    }


    @SneakyThrows
    public static List<Operation> operations(String path) {
        OperationsResponse.Builder builder = OperationsResponse.newBuilder();
        var content = FileUtils.readFileToString(new File(ResourceUtils.getFile(path).getAbsolutePath()));
        JsonFormat.parser().merge(content, builder);
        return builder.build().getOperationsList();
    }
}
