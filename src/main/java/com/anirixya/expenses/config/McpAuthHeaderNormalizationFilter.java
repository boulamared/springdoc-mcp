package com.anirixya.expenses.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Normalizes Authorization headers sent by tools like MCP Inspector
 * which automatically prepend 'Bearer ' even when Basic auth credentials are provided.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class McpAuthHeaderNormalizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer Basic ", 0, 13)) {
            final String normalized = authHeader.substring(7); // Strips "Bearer " -> "Basic ..."
            HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)) {
                        return normalized;
                    }
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)) {
                        return Collections.enumeration(List.of(normalized));
                    }
                    return super.getHeaders(name);
                }
            };
            filterChain.doFilter(wrapped, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
