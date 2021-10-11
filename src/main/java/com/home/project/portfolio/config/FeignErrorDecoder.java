package com.home.project.portfolio.config;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Custom error decoding for enabling retry
 */
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String s, Response response) {
        Exception exception = defaultErrorDecoder.decode(s, response);

        if (exception instanceof RetryableException) {
            return exception;
        }
        if (HttpStatus.valueOf(response.status()).is5xxServerError()) {
            return new RetryableException(response.status(), "Tinkoff server error", response.request().httpMethod(),
                    null, null, response.request());
        }

        return exception;
    }
}
