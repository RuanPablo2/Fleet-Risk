package com.ruanpablo2.fleet_gateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public AuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/api/v1/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied: Missing token or invalid format");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isValid(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied: Invalid or expired token");
            return;
        }

        String rawBrokerName = jwtUtil.extractBrokerName(token);
        String cnpj = jwtUtil.extractCnpj(token);

        String safeBrokerName = URLEncoder.encode(rawBrokerName, StandardCharsets.UTF_8);

        System.out.println("🔐 [GATEWAY] Token validated! Forwarding broker's request: " + rawBrokerName);

        HttpServletRequestWrapper mutatedRequest = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if ("X-Broker-Name".equalsIgnoreCase(name)) return safeBrokerName;
                if ("X-Broker-Cnpj".equalsIgnoreCase(name)) return cnpj;
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if ("X-Broker-Name".equalsIgnoreCase(name)) return Collections.enumeration(List.of(safeBrokerName));
                if ("X-Broker-Cnpj".equalsIgnoreCase(name)) return Collections.enumeration(List.of(cnpj));
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = Collections.list(super.getHeaderNames());
                names.add("X-Broker-Name");
                names.add("X-Broker-Cnpj");
                return Collections.enumeration(names);
            }
        };

        filterChain.doFilter(mutatedRequest, response);
    }
}