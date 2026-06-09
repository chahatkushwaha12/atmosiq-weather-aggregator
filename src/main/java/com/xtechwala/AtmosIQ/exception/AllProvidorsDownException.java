package com.xtechwala.AtmosIQ.exception;

public class AllProvidorsDownException extends RuntimeException {
    public AllProvidorsDownException() {
        super("All weather providers are currently unavailable. Please try again later.");
    }
}
