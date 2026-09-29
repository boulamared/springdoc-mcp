package com.anirixya.expenses.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.ai.customizers.McpToolCustomizer;
import org.springdoc.ai.customizers.McpToolDefinitionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class McpGovernanceTest {

    @Autowired
    private McpToolCustomizer expenseToolCustomizer;

    @Test
    @DisplayName("Governance: McpToolCustomizer excludes internal admin endpoints from tool discovery")
    void shouldExcludeInternalAdminEndpoints() {
        McpToolDefinitionContext context = new McpToolDefinitionContext("adminDiagnostics", "Internal diagnostics", "{}");
        Operation op = new Operation().operationId("adminDiagnostics");

        McpToolDefinitionContext result = expenseToolCustomizer.customize(
            context,
            "/api/v1/admin/internal/diagnostics",
            PathItem.HttpMethod.GET,
            op
        );

        assertThat(result.isExclude()).isTrue();
    }

    @Test
    @DisplayName("Governance: McpToolCustomizer keeps standard business endpoints exposed")
    void shouldKeepBusinessEndpointsExposed() {
        McpToolDefinitionContext context = new McpToolDefinitionContext("submitExpense", "Submit expense", "{}");
        Operation op = new Operation().operationId("submitExpense");

        McpToolDefinitionContext result = expenseToolCustomizer.customize(
            context,
            "/api/v1/expenses",
            PathItem.HttpMethod.POST,
            op
        );

        assertThat(result.isExclude()).isFalse();
    }

    @Test
    @DisplayName("Governance: McpToolCustomizer appends critical safety policy warning to DELETE operations")
    void shouldAppendCriticalSafetyWarningToDestructiveOperations() {
        McpToolDefinitionContext context = new McpToolDefinitionContext(
            "deleteExpense",
            "Delete an expense by ID",
            "{}"
        );
        Operation op = new Operation().operationId("deleteExpense");

        McpToolDefinitionContext result = expenseToolCustomizer.customize(
            context,
            "/api/v1/expenses/{id}",
            PathItem.HttpMethod.DELETE,
            op
        );

        assertThat(result.getDescription())
            .contains("Delete an expense by ID")
            .contains("[CRITICAL SAFETY POLICY]")
            .contains("Always prompt the human user for explicit textual confirmation before calling.");
    }

    @Test
    @DisplayName("Governance: McpToolCustomizer classifies GET operations as safe endpoints")
    void shouldMarkGetOperationsAsSafe() {
        McpToolDefinitionContext context = new McpToolDefinitionContext("listExpenses", "List expenses", "{}");
        Operation op = new Operation().operationId("listExpenses");

        McpToolDefinitionContext result = expenseToolCustomizer.customize(
            context,
            "/api/v1/expenses",
            PathItem.HttpMethod.GET,
            op
        );

        assertThat(result.getSafeEndpoint()).isTrue();
    }
}
