package com.home.project.portfolio.helpers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.home.project.portfolio.model.operations.Operations;
import com.home.project.portfolio.model.portfolio.Portfolio;
import com.home.project.portfolio.model.portfolio.Position;
import lombok.val;

import java.io.File;

import static org.junit.jupiter.api.Assertions.fail;

public class TestUtils {

    public static <T> T readValue(String resourcePath, Class<T> clazz) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        mapper.registerModule(new JavaTimeModule());

        return mapper.readValue(new File(resourcePath), clazz);
    }

    public static Operations readOperations() {
        String resourcePath = "src/test/resources/testData/mockOperations.json";
        try {
            return readValue(resourcePath, Operations.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read operations file");
        }
    }

    public static Portfolio readPositions() {
        String resourcePath = "src/test/resources/testData/positions.json";
        try {
            return readValue(resourcePath, Portfolio.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read positions file");
        }
    }
}
