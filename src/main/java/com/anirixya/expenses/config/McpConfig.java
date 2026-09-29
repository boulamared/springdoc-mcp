package com.anirixya.expenses.config;

import io.swagger.v3.oas.models.PathItem;
import org.springdoc.ai.customizers.McpToolCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public McpToolCustomizer expenseToolCustomizer() {
        return (context, path, method, operation) -> {
            if (path.startsWith("/api/v1/admin") || (context.getName() != null && context.getName().startsWith("admin"))) {
                context.setExclude(true);
                return context;
            }

            if (method == PathItem.HttpMethod.DELETE) {
                String existing = context.getDescription() != null ? context.getDescription() : "";
                context.setDescription(existing +
                    "\n[CRITICAL SAFETY POLICY]: This operation permanently purges financial records. " +
                    "Always prompt the human user for explicit textual confirmation before calling."
                );
            }

            if (method == PathItem.HttpMethod.GET) {
                context.setSafeEndpoint(true);
            }

            return context;
        };
    }
}
