package com.anirixya.expenses.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/internal")
@Tag(name = "Internal Administration", description = "Internal management and diagnostics endpoints")
public class AdminController {

    @GetMapping("/diagnostics")
    @Operation(
        operationId = "adminDiagnostics",
        summary = "Internal system diagnostics",
        description = "Internal administrative tool - should NOT be exposed to LLM agents."
    )
    public ResponseEntity<Map<String, String>> diagnostics() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "environment", "production",
            "cluster", "primary-01"
        ));
    }
}
