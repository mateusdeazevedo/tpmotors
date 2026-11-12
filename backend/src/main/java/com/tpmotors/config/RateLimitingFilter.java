package com.tpmotors.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mecanismo 2 de Segurança:
 * Rate Limiting e Firewall com IP Allow List.
 * Bloqueia requisições excedentes por IP retornando HTTP 429.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${tpmotors.rate-limit.max-requests-per-minute:60}")
    private int maxRequestsPerMinute;

    @Value("${tpmotors.rate-limit.allow-list:127.0.0.1,0:0:0:0:0:0:0:1}")
    private String allowListConfig;

    private static class IpCounter {
        final AtomicInteger count = new AtomicInteger(0);
        long windowStart = System.currentTimeMillis();
    }

    private final Map<String, IpCounter> ipRequestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = extrairClientIp(request);
        List<String> allowList = Arrays.asList(allowListConfig.split(","));

        // Se estiver na lista de permissão estrita de bypass de rate-limit (ex: monitoramento parceiro)
        boolean isWhitelisted = allowList.stream()
                .anyMatch(ip -> ip.trim().equalsIgnoreCase(clientIp));

        if (!isWhitelisted && !isClientWithinLimit(clientIp)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Retry-After", "60");
            response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequestsPerMinute));
            response.setHeader("X-RateLimit-Remaining", "0");

            String jsonResponse = """
                    {
                        "timestamp": "%s",
                        "status": 429,
                        "error": "Too Many Requests",
                        "message": "Taxa de requisições por IP excedida (Rate Limit). Limite: %d req/min. Tente novamente mais tarde.",
                        "clientIp": "%s"
                    }
                    """.formatted(java.time.Instant.now().toString(), maxRequestsPerMinute, clientIp);

            response.getWriter().write(jsonResponse);
            return;
        }

        // Segue a cadeia de filtros
        filterChain.doFilter(request, response);
    }

    private boolean isClientWithinLimit(String clientIp) {
        long agora = System.currentTimeMillis();
        IpCounter counter = ipRequestCounts.computeIfAbsent(clientIp, k -> new IpCounter());

        synchronized (counter) {
            // Janela de 1 minuto (60.000 ms)
            if (agora - counter.windowStart > 60000) {
                counter.windowStart = agora;
                counter.count.set(1);
                return true;
            }

            int currentCount = counter.count.incrementAndGet();
            return currentCount <= maxRequestsPerMinute;
        }
    }

    private String extrairClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            xfHeader = request.getHeader("Proxy-Client-IP");
        }
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            xfHeader = request.getHeader("WL-Proxy-Client-IP");
        }
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
