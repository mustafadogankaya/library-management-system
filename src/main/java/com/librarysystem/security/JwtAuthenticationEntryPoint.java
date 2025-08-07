package com.librarysystem.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

/**
 * JWT Authentication Entry Point for handling authentication failures.
 * Returns standardized error response for unauthorized requests.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = -7858869558953243875L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        // Set response status and headers
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Create standardized error response (no sensitive information)
        String jsonResponse = "{"
                + "\"error\": \"Unauthorized\","
                + "\"message\": \"Access denied. Please provide valid authentication credentials.\","
                + "\"status\": 401"
                + "}";
        
        response.getWriter().write(jsonResponse);
    }
}