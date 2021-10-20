package com.home.project.portfolio.model.operations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Operations {
    private String trackingId;
    private Payload payload;
    private String status;

    @Data
    public static class Payload {
        private List<Operation> operations;
    }
}
