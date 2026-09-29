package com.anirixya.expenses.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                .requestMatchers("/mcp", "/v3/api-docs/mcp", "/sse", "/mcp/message").hasAnyAuthority("SCOPE_mcp.connect", "ROLE_MCP_CLIENT")

                // Human-in-the-Loop Admin Dashboard and management endpoints require elevated privileges
                .requestMatchers("/mcp-ui/**", "/api/mcp-admin/**").hasRole("FINANCE_ADMIN")

                .requestMatchers("/api/v1/expenses/**").authenticated()

                .requestMatchers("/api/v1/admin/**").hasRole("FINANCE_ADMIN")

                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
            .username("admin")
            .password("{noop}admin123")
            .roles("FINANCE_ADMIN")
            .authorities("ROLE_FINANCE_ADMIN", "SCOPE_mcp.connect")
            .build();

        UserDetails agent = User.builder()
            .username("ai-agent")
            .password("{noop}agent123")
            .roles("MCP_CLIENT")
            .authorities("ROLE_MCP_CLIENT", "SCOPE_mcp.connect")
            .build();

        UserDetails user = User.builder()
            .username("employee")
            .password("{noop}user123")
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(admin, agent, user);
    }

    /**
     * Provide a fallback JwtDecoder bean for testing and standalone demonstration
     * when an external OAuth2 authorization server / JWKS URL is not configured.
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder() {
        return token -> Jwt.withTokenValue(token)
            .header("alg", "none")
            .claim("sub", "ai-agent")
            .claim("scope", "mcp.connect")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
    }
}
