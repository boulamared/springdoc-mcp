package com.anirixya.expenses.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Payload required to record a corporate expense")
public record ExpenseDto(

    @Schema(description = "Total expenditure amount", example = "78.50")
    @Positive(message = "Expense amount must be strictly greater than zero")
    BigDecimal amount,

    @Schema(description = "Three-letter ISO currency code", example = "USD")
    @NotBlank(message = "Currency code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    String currency,

    @Schema(description = "Expense category. Permitted values: TRAVEL, MEALS, HARDWARE, SOFTWARE, OFFICE", example = "MEALS")
    @NotBlank(message = "Category is required")
    String category
) {}
