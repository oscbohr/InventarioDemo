package com.inventory.activos.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Asigna un ID a cada petición y lo publica en el MDC (clave {@code requestId}) para el segmento
 * personalizado del log (ver {@code logging.pattern.level} en application.yml). Se devuelve en
 * el header {@code X-Request-Id}; si el cliente envía uno válido, se reutiliza.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    private static final Pattern VALIDO = Pattern.compile("[A-Za-z0-9-]{1,64}");
    private static final Logger log = LoggerFactory.getLogger(RequestIdFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String id = idDe(request);
        MDC.put(MDC_KEY, id);
        response.setHeader(HEADER, id);
        try {
            chain.doFilter(request, response);
        } finally {
            log.info("{} {} -> {}", request.getMethod(), request.getRequestURI(), response.getStatus());
            MDC.remove(MDC_KEY);
        }
    }

    /** Solo se acepta un ID del cliente con caracteres seguros, para evitar inyección en los logs. */
    private static String idDe(HttpServletRequest request) {
        String recibido = request.getHeader(HEADER);
        if (recibido != null && VALIDO.matcher(recibido).matches()) {
            return recibido;
        }
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
