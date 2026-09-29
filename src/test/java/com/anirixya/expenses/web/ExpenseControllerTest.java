package com.anirixya.expenses.web;

import com.anirixya.expenses.dto.ExpenseDto;
import com.anirixya.expenses.dto.ExpenseRecord;
import com.anirixya.expenses.service.ExpenseApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExpenseApplicationService expenseService;

    @Test
    @DisplayName("Should successfully create expense and return HTTP 201 with generated UUID")
    void shouldCreateExpenseSuccessfully() throws Exception {
        ExpenseDto dto = new ExpenseDto(new BigDecimal("78.50"), "USD", "MEALS");

        mockMvc.perform(post("/api/v1/expenses")
                .with(httpBasic("ai-agent", "agent123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.amount").value(78.50))
            .andExpect(jsonPath("$.currency").value("USD"))
            .andExpect(jsonPath("$.category").value("MEALS"))
            .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("Should reject negative expense with HTTP 400 and structured self-correction field error")
    void shouldRejectNegativeExpenseAmountForSelfCorrection() throws Exception {
        ExpenseDto dto = new ExpenseDto(new BigDecimal("-50.00"), "USD", "MEALS");

        mockMvc.perform(post("/api/v1/expenses")
                .with(httpBasic("ai-agent", "agent123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.fieldErrors", hasSize(1)))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("amount"))
            .andExpect(jsonPath("$.fieldErrors[0].message").value("Expense amount must be strictly greater than zero"))
            .andExpect(jsonPath("$.fieldErrors[0].rejectedValue").value(-50.00));
    }

    @Test
    @DisplayName("Should reject invalid currency code with HTTP 400 constraint violation")
    void shouldRejectInvalidCurrencyCode() throws Exception {
        ExpenseDto dto = new ExpenseDto(new BigDecimal("120.00"), "INVALID_CURRENCY", "TRAVEL");

        mockMvc.perform(post("/api/v1/expenses")
                .with(httpBasic("ai-agent", "agent123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.fieldErrors", hasSize(1)))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("currency"))
            .andExpect(jsonPath("$.fieldErrors[0].message").value("Currency must be a valid 3-letter ISO code"));
    }

    @Test
    @DisplayName("Should retrieve list of expenses")
    void shouldListExpenses() throws Exception {
        expenseService.submitExpense(new ExpenseDto(new BigDecimal("42.00"), "EUR", "TRAVEL"));

        mockMvc.perform(get("/api/v1/expenses")
                .with(httpBasic("employee", "user123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @DisplayName("Should retrieve single expense by ID")
    void shouldGetExpenseById() throws Exception {
        ExpenseRecord created = expenseService.submitExpense(new ExpenseDto(new BigDecimal("99.99"), "USD", "HARDWARE"));

        mockMvc.perform(get("/api/v1/expenses/{id}", created.id())
                .with(httpBasic("employee", "user123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(created.id().toString()))
            .andExpect(jsonPath("$.amount").value(99.99))
            .andExpect(jsonPath("$.category").value("HARDWARE"));
    }

    @Test
    @DisplayName("Should delete expense and return HTTP 204")
    void shouldDeleteExpense() throws Exception {
        ExpenseRecord created = expenseService.submitExpense(new ExpenseDto(new BigDecimal("15.00"), "USD", "OFFICE"));

        mockMvc.perform(delete("/api/v1/expenses/{id}", created.id())
                .with(httpBasic("employee", "user123")))
            .andExpect(status().isNoContent());

        // Verify subsequent fetch returns 404
        mockMvc.perform(get("/api/v1/expenses/{id}", created.id())
                .with(httpBasic("employee", "user123")))
            .andExpect(status().isNotFound());
    }
}
