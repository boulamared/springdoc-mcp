package com.anirixya.expenses.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Standard structured error response facilitating autonomous AI agent self-correction")
public record ErrorResponse(

    @Schema(description = "HTTP status code", example = "400")
    int status,

    @Schema(description = "High-level error classification", example = "Bad Request")
    String error,

    @Schema(description = "Diagnostic message explaining the violation")
    String message,

    @Schema(description = "List of specific field violations for parameter self-correction")
    List<FieldErrorDetail> fieldErrors,

    @Schema(description = "Error occurrence timestamp")
    Instant timestamp
) {
    public static ErrorResponse of(int status, String error, String message, List<FieldErrorDetail> fieldErrors) {
        return new ErrorResponse(status, error, message, fieldErrors, Instant.now());
    }

    public record FieldErrorDetail(
        @Schema(description = "Name of the invalid parameter/field", example = "amount")
        String field,

        @Schema(description = "Explanation of constraint requirement", example = "Expense amount must be strictly greater than zero")
        String message,

        @Schema(description = "The rejected value sent by the agent", example = "-50.00")
        Object rejectedValue
    ) {}
}
