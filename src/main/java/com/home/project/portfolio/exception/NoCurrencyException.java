package com.home.project.portfolio.exception;

/**
 * @author rlagay
 */
public class NoCurrencyException extends RuntimeException {

    public NoCurrencyException() {
        super("Currency is not recognized");
    }
}
