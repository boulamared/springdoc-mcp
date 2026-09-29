package com.anirixya.expenses.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Expense {

    private final UUID id;
    private final BigDecimal amount;
    private final String currency;
    private final String category;
    private ExpenseStatus status;
    private final Instant createdAt;

    public Expense(UUID id, BigDecimal amount, String currency, String category, ExpenseStatus status, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
        this.category = Objects.requireNonNull(category, "category must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public static Expense create(BigDecimal amount, String currency, String category) {
        return new Expense(
            UUID.randomUUID(),
            amount,
            currency.toUpperCase(),
            category.toUpperCase(),
            ExpenseStatus.PENDING_APPROVAL,
            Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCategory() {
        return category;
    }

    public ExpenseStatus getStatus() {
        return status;
    }

    public void setStatus(ExpenseStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
