package com.flightbookingapp.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/** Standardised error envelope returned by the global exception handler. */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {

    @Builder.Default
    private LocalDateTime       timestamp = LocalDateTime.now();
    private int                 status;
    private String              error;
    private String              message;
    private String              path;
    /** Per-field validation errors from @Valid constraints. */
    private Map<String, String> fieldErrors;
}
