package com.seguros.polizas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Seguridad mínima: exige el header x-api-key en todas las peticiones,
 * excepto la consola de H2 (solo para desarrollo).
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER = "x-api-key";

    private final String apiKeyEsperada;

    public ApiKeyFilter(@Value("${seguridad.api-key}") String apiKeyEsperada) {
        this.apiKeyEsperada = apiKeyEsperada;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!apiKeyEsperada.equals(request.getHeader(HEADER))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\":401,\"error\":\"No autorizado\",\"mensaje\":\"Header x-api-key ausente o inválido\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
