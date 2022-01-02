package com.home.project.portfolio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class IncorrectDurationException extends HttpClientErrorException {
    public IncorrectDurationException(HttpStatus statusCode, String statusText) {
        super(statusCode, statusText);
    }
}
