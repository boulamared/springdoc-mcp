# Spring Boot 4.x & Java 21 Zero-Code MCP Server with Springdoc

> **Companion Project for the Article:**  
> *"The Zero-Code AI Agent: Bridging Spring Boot and LLMs with OpenAPI"*

This repository demonstrates how to turn standard Spring Boot REST controllers into enterprise-ready **Model Context Protocol (MCP)** tools for autonomous AI agents (Claude Desktop, Cursor, etc.) using `springdoc-openapi-starter-webmvc-mcp:3.1.1` on **Spring Boot 4.x** and **Java 21**.

---

## Key Architectural Highlights

1. **Eliminate the "AI Maintenance Tax"**:
   - Zero duplicated `@Tool` methods or bespoke AI wrapper classes.
   - Your existing `@RestController` is the AI tool.
   - OpenAPI metadata (`operationId`, `@Operation`, `@Tag`) acts as the AI runtime system prompt.

2. **Autonomous AI Self-Correction**:
   - Request payloads are defined with **Java 21 Records** and Jakarta Bean Validation (`@Positive`, `@NotBlank`, `@Pattern`).
   - Invalid payloads from LLMs automatically trigger HTTP 400 Bad Request with structured field diagnostics (`field`, `message`, `rejectedValue`).
   - LLMs natively read this feedback and self-correct their subsequent calls without custom retry loops.

3. **Level 2 Runtime Governance (`McpToolCustomizer`)**:
   - Dynamically inspect and filter endpoints before exposing them to the agent.
   - Exclude internal administrative utilities (`/api/v1/admin/**`) via `context.setExclude(true)`.
   - Inject critical safety warnings into prompt descriptions for destructive calls (`DELETE`).
   - Provide AI-optimized descriptions and tool names via `@McpToolDescription`.

4. **Enterprise Human-in-the-Loop (HITL) Guardrails**:
   - Configurable classification of safe (`GET`, `HEAD`, `OPTIONS`) vs. mutating (`POST`, `PUT`, `DELETE`) actions.
   - Mutating calls automatically require human administrative approval before execution.
   - Built-in audit logging (`org.springdoc.ai.mcp.audit`) with secret redaction (`springdoc.ai.mcp.audit.redact=true`).

5. **Unified Spring Security**:
   - Protects the AI protocol endpoint (`/mcp`) via OAuth2 scopes (`SCOPE_mcp.connect`).
   - Protects the MCP Admin Dashboard (`/mcp-ui/**` and `/api/mcp-admin/**`) with elevated role checks (`FINANCE_ADMIN`).
   - Secures domain business APIs with standard enterprise policies.

---

## Tech Stack

- **Java**: 21 LTS (Virtual Threads enabled: `spring.threads.virtual.enabled: true`)
- **Spring Boot**: 4.1.0
- **Springdoc OpenAPI**: 3.1.1 (`springdoc-openapi-starter-webmvc-mcp`, `springdoc-openapi-starter-webmvc-ui`)
- **Spring Security**: 7.x / OAuth2 Resource Server
- **Jakarta Bean Validation**: Hibernate Validator
- **Testing**: JUnit 5, MockMvc, AssertJ, Spring Security Test

---

## Project Structure

```
├── pom.xml
├── README.md
├── article-medium.md
├── src/
│   ├── main/
│   │   ├── java/com/anirixya/expenses/
│   │   │   ├── ExpenseApplication.java
│   │   │   ├── config/
│   │   │   │   ├── McpConfig.java             # McpToolCustomizer bean
│   │   │   │   ├── OpenApiConfig.java         # OpenAPI spec definition & Jackson config
│   │   │   │   └── SecurityConfig.java        # Spring Security & OAuth2 configuration
│   │   │   ├── domain/
│   │   │   │   ├── Expense.java               # Domain model
│   │   │   │   ├── ExpenseCategory.java       # Category enum
│   │   │   │   └── ExpenseStatus.java         # Status enum
│   │   │   ├── dto/
│   │   │   │   ├── ExpenseDto.java            # Java 21 Record with Jakarta constraints
│   │   │   │   ├── ExpenseRecord.java         # Immutable response record
│   │   │   │   └── ErrorResponse.java         # Structured error response for AI self-correction
│   │   │   ├── repository/
│   │   │   │   └── ExpenseRepository.java     # Thread-safe in-memory repository
│   │   │   ├── service/
│   │   │   │   └── ExpenseApplicationService.java
│   │   │   └── web/
│   │   │       ├── ExpenseController.java     # REST API exposed as MCP tools
│   │   │       ├── AdminController.java       # Internal admin controller (excluded)
│   │   │       └── GlobalExceptionHandler.java # Validation exception handler
│   │   └── resources/
│   │       └── application.yml                # Virtual threads, MCP settings, guardrails
│   └── test/
│       └── java/com/anirixya/expenses/
│           ├── ExpenseApplicationTests.java
│           ├── config/McpGovernanceTest.java      # McpToolCustomizer unit tests
│           ├── security/SecurityConfigTest.java  # Security slice tests
│           ├── web/ExpenseControllerTest.java    # REST API & self-correction tests
│           └── web/McpDashboardAndHitlTest.java  # MCP Tools discovery & HITL tests
```

---

## Getting Started

### Prerequisites

- **Java 21** or later
- **Maven 3.9+**

### Building and Running Tests

```bash
mvn clean test
```

### Running the Application

```bash
mvn spring-boot:run
```

The server starts on port `8080`.

---

## Interactive Endpoints & Credentials

### Default Test Credentials (In-Memory)
- **Finance Admin**: `admin` / `admin123` (Roles: `FINANCE_ADMIN`, `SCOPE_mcp.connect`)
- **AI Agent**: `ai-agent` / `agent123` (Roles: `MCP_CLIENT`, `SCOPE_mcp.connect`)
- **Employee**: `employee` / `user123` (Roles: `USER`)

### Available URLs
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **MCP Developer Dashboard**: [http://localhost:8080/mcp-ui](http://localhost:8080/mcp-ui) (Log in as `admin` / `admin123`)
- **MCP Tool List API**: [http://localhost:8080/api/mcp-admin/tools](http://localhost:8080/api/mcp-admin/tools)
- **MCP Protocol Endpoint**: `http://localhost:8080/mcp`
- **MCP Native SSE Endpoint**: `http://localhost:8080/sse`
- **MCP SSE Message Endpoint**: `http://localhost:8080/mcp/message`

---

## AI Agent & MCP Inspector Integration (SSE Transport)

### 1. Launching the MCP Inspector
Launch the official MCP Inspector in your terminal:
```bash
npx @modelcontextprotocol/inspector
```
Once the Inspector UI opens in your browser (at `http://localhost:5173`):
1. **Transport Type**: Select **`SSE`**.
2. **URL**: Enter `http://localhost:8080/sse`.
3. **Headers / Authentication**:
   Add the Authorization header with Basic Auth for the `ai-agent`:
   ```
   Authorization: Basic YWktYWdlbnQ6YWdlbnQxMjM=
   ```
4. Click **Connect**. The inspector will load the auto-generated tools (`submit_expense`, `list_expenses`, `find_expense_by_id`, `delete_expense`).

### 2. End-to-End Human-in-the-Loop (HITL) Testing Flow
Because the application is configured with `springdoc.ai.mcp.guardrails.require-approval-for-mutating-tools=true`, any mutating actions (POST, PUT, DELETE) are intercepted. Here is how to test this workflow:

1. **Invoke a Mutating Tool via Inspector**: 
   - In the MCP Inspector, select the `delete_expense` tool.
   - Enter an ID parameter (e.g., `id: 1`) and click **Run Tool**.
   - **Observe the Result**: The request will *not* be executed immediately. The tool response will show `"requiresApproval": true` (or similar) and a log event is created.
   
2. **Log into the Admin Dashboard**:
   - Open a new browser tab to the MCP Admin UI at [http://localhost:8080/mcp-ui](http://localhost:8080/mcp-ui).
   - Log in with the Finance Admin credentials: `admin` / `admin123`.
   
3. **Approve the Intercepted Request**:
   - Navigate to the **Audit / Governance** tab in the Admin UI.
   - You will see the intercepted `delete_expense` tool call marked as requiring approval.
   - Click **Approve**. The dashboard will then finalize and execute the original destructive request on behalf of the AI agent.

---

### Claude Desktop Configuration (SSE)

Add the following to your `claude_desktop_config.json` (on macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`):

```json
{
  "mcpServers": {
    "spring-boot-expenses": {
      "url": "http://localhost:8080/sse",
      "headers": {
        "Authorization": "Basic YWktYWdlbnQ6YWdlbnQxMjM="
      }
    }
  }
}
```

Restart Claude Desktop, and the tools (`submit_expense`, `list_expenses`, `find_expense_by_id`, `delete_expense`) will automatically appear ready for use.

---

## License

Apache License 2.0
