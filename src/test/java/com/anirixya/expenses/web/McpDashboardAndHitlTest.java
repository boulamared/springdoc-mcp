package com.anirixya.expenses.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class McpDashboardAndHitlTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("MCP Tools API: Lists all registered tools and excludes admin endpoints")
    void shouldListRegisteredMcpTools() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/mcp-admin/tools")
                .with(httpBasic("admin", "admin123")))
            .andExpect(status().isOk())
            .andReturn();

        String json = result.getResponse().getContentAsString();
        JsonNode tools = objectMapper.readTree(json);

        assertThat(tools.isArray()).isTrue();
        assertThat(tools.size()).isEqualTo(4);

        // Verify tool names
        boolean hasSubmit = false;
        boolean hasList = false;
        boolean hasFindById = false;
        boolean hasDelete = false;
        boolean hasAdmin = false;

        for (JsonNode tool : tools) {
            String name = tool.path("name").asText();
            if ("submit_expense".equals(name) || "submitExpense".equals(name)) hasSubmit = true;
            if ("list_expenses".equals(name) || "listExpenses".equals(name)) hasList = true;
            if ("find_expense_by_id".equals(name)) hasFindById = true;
            if ("delete_expense".equals(name) || "deleteExpense".equals(name)) hasDelete = true;
            if (name.toLowerCase().contains("admin")) hasAdmin = true;
        }

        assertThat(hasSubmit).as("Should have submit expense tool").isTrue();
        assertThat(hasList).as("Should have list expenses tool").isTrue();
        assertThat(hasFindById).as("Should have @McpToolDescription renamed tool find_expense_by_id").isTrue();
        assertThat(hasDelete).as("Should have delete expense tool").isTrue();
        assertThat(hasAdmin).as("Admin endpoint should be excluded by McpToolCustomizer").isFalse();
    }

    @Test
    @DisplayName("HITL Guardrail: Mutating method submit_expense requires human approval before execution")
    void shouldRequireApprovalForMutatingToolWithoutHumanApproval() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/mcp-admin/tools/execute")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "toolName", "submit_expense",
                    "arguments", "{\"amount\": 85.00, \"currency\": \"USD\", \"category\": \"MEALS\"}",
                    "approved", false
                ))))
            .andExpect(status().isOk())
            .andReturn();

        String json = result.getResponse().getContentAsString();
        JsonNode resp = objectMapper.readTree(json);
        // Intercepted by guardrail
        assertThat(resp.path("requiresApproval").asBoolean()).isTrue();
    }
}
