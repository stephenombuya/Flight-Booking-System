package com.flightbookingapp.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Uniform error envelope returned for all error responses.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String timestamp;
    private int    status;
    private String error;
    private String message;
    private String path;
    /** Present only for validation errors — maps field name → message. */
    private Map<String, String> fieldErrors;
}
