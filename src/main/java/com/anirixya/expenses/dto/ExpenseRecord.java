package com.anirixya.expenses.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Persisted expense record")
public record ExpenseRecord(

    @Schema(description = "Unique expense tracking identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "Total expenditure amount", example = "78.50")
    BigDecimal amount,

    @Schema(description = "Three-letter ISO currency code", example = "USD")
    String currency,

    @Schema(description = "Expense category", example = "MEALS")
    String category,

    @Schema(description = "Timestamp when the expense was submitted")
    Instant createdAt
) {
    public ExpenseRecord(UUID id, BigDecimal amount, String currency, String category) {
        this(id, amount, currency, category, Instant.now());
    }
}
