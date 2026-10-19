package com.tarun.nest.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void returnsSafeMessageForDatabaseConstraintFailure() {
        HttpServletRequest request = new MockHttpServletRequest(
                "POST", "/api/orders/3/payment"
        );
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "null value in column product_id violates not-null constraint"
                );

        var response = handler.handleDataIntegrityViolation(exception, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(
                "We couldn't complete your request right now. Please try again in a few minutes. "
                        + "If the problem continues, contact support.",
                response.getBody().getMessage()
        );
        assertEquals("/api/orders/3/payment", response.getBody().getPath());
    }

    @Test
    void doesNotExposeUnhandledExceptionDetails() {
        HttpServletRequest request = new MockHttpServletRequest(
                "POST", "/api/orders/3/payment"
        );
        IllegalStateException exception =
                new IllegalStateException("database connection string");

        var response = handler.handleException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(
                "An unexpected error occurred. Please try again later.",
                response.getBody().getMessage()
        );
    }
}
