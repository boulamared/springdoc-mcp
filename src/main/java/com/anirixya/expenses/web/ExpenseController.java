package com.anirixya.expenses.web;

import com.anirixya.expenses.dto.ExpenseDto;
import com.anirixya.expenses.dto.ExpenseRecord;
import com.anirixya.expenses.service.ExpenseApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.ai.annotations.McpToolDescription;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
@Tag(name = "Expense Management", description = "Core tools for corporate expense administration")
public class ExpenseController {

    private final ExpenseApplicationService expenseService;

    public ExpenseController(ExpenseApplicationService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        operationId = "submitExpense",
        summary = "Submit a corporate expense",
        description = "Use this tool to submit an employee expense report. " +
                      "Accurately extract the total amount, currency code, and category from the prompt."
    )
    public ResponseEntity<ExpenseRecord> submitExpense(@RequestBody @Valid ExpenseDto dto) {
        ExpenseRecord record = expenseService.submitExpense(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @GetMapping
    @Operation(
        operationId = "listExpenses",
        summary = "List corporate expenses",
        description = "Use this tool to query all recorded expenses in the system. Returns a list of expense summaries."
    )
    public ResponseEntity<List<ExpenseRecord>> listExpenses() {
        return ResponseEntity.ok(expenseService.listAllExpenses());
    }

    @GetMapping("/{id}")
    @Operation(
        operationId = "getExpenseById",
        summary = "Retrieve an expense by ID",
        description = "Query an existing expense report by its unique UUID."
    )
    @McpToolDescription(
        value = "Look up a single corporate expense record by its unique UUID identifier",
        name = "find_expense_by_id"
    )
    public ResponseEntity<ExpenseRecord> getExpenseById(
        @Parameter(description = "The UUID of the expense record", required = true)
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(expenseService.getExpense(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        operationId = "deleteExpense",
        summary = "Delete an expense",
        description = "Permanently remove an expense record from the ledger using its UUID."
    )
    public ResponseEntity<Void> deleteExpense(
        @Parameter(description = "The UUID of the expense to delete", required = true)
        @PathVariable UUID id
    ) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
