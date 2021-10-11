package com.home.project.portfolio.model.operations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Operations {
    private String trackingId;
    private Payload payload;
    private String status;
}
