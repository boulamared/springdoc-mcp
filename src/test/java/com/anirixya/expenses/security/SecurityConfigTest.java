package com.anirixya.expenses.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Security: Public access to OpenAPI documentation is allowed")
    void shouldAllowPublicAccessToOpenApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Security: Unauthenticated access to MCP protocol endpoint is rejected with HTTP 401")
    void shouldRejectUnauthenticatedAccessToMcpEndpoint() throws Exception {
        mockMvc.perform(get("/mcp"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: Unauthenticated access to MCP SSE endpoint is rejected with HTTP 401")
    void shouldRejectUnauthenticatedAccessToSseEndpoint() throws Exception {
        mockMvc.perform(get("/sse"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: Authorized AI agent with SCOPE_mcp.connect can connect to SSE endpoint")
    void shouldAllowAuthorizedAgentAccessToSseEndpoint() throws Exception {
        MvcResult result = mockMvc.perform(get("/sse")
                .with(httpBasic("ai-agent", "agent123")))
            .andReturn();
        int statusCode = result.getResponse().getStatus();
        assertThat(statusCode).isNotIn(401, 403);
    }

    @Test
    @DisplayName("Security: Regular employee user without FINANCE_ADMIN is forbidden from accessing MCP Dashboard")
    void shouldForbidRegularUserFromAccessingMcpDashboard() throws Exception {
        mockMvc.perform(get("/mcp-ui")
                .with(httpBasic("employee", "user123")))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security: Admin with FINANCE_ADMIN can access MCP Dashboard")
    void shouldAllowAdminAccessToMcpDashboard() throws Exception {
        MvcResult result = mockMvc.perform(get("/mcp-ui")
                .with(httpBasic("admin", "admin123")))
            .andReturn();
        int statusCode = result.getResponse().getStatus();
        assertThat(statusCode).isNotIn(401, 403);
    }
}
