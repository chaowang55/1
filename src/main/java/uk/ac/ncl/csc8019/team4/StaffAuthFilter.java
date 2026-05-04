package uk.ac.ncl.csc8019.team4;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Protects staff-only endpoints by requiring a secret token in the
 * "X-Staff-Token" request header.
 *
 * The token is set in application.properties:
 *   staff.secret-token=changeme
 *
 * Staff-only paths:
 *   GET  /api/menu/all
 *   POST /api/menu
 *   PUT  /api/menu/{id}
 *   PATCH /api/menu/{id}/availability
 *   DELETE /api/menu/{id}
 *   GET  /api/orders/dashboard
 *   GET  /api/orders/archive
 *   PATCH /api/orders/{id}/status
 *   PATCH /api/orders/{id}/cancel
 *   POST /api/orders/{id}/payments/mark-paid
 *   PATCH /api/locations/{id}/status
 *   PUT /api/opening-hours/{dayOfWeek}
 */
@Component
public class StaffAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Staff-Token";

    // Paths that require the staff token
    private static final List<String> STAFF_PATHS =
            List.of("/api/menu/all", "/api/orders/dashboard", "/api/orders/archive");

    @Value("${staff.secret-token:test-token}")
    private String secretToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Allow browser CORS preflight requests through.
        // The real GET/POST/PATCH request will still be checked for X-Staff-Token.
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        boolean isStaffPath = isStaffOnlyRequest(path, method);

        if (isStaffPath) {
            String token = request.getHeader(HEADER);

            if (token == null || !token.equals(secretToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorised: staff token required\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isStaffOnlyRequest(String path, String method) {
        // Exact staff-only GET paths
        if (STAFF_PATHS.contains(path)) return true;

        // Any write to /api/menu (POST, PUT, PATCH, DELETE)
        if (path.startsWith("/api/menu") && !method.equals("GET")) return true;

        // Any write to /api/locations (POST, PUT, PATCH, DELETE)
        if (path.startsWith("/api/locations") && !method.equals("GET")) return true;

        // Any write to /api/opening-hours (POST, PUT, PATCH, DELETE)
        if (path.startsWith("/api/opening-hours") && !method.equals("GET")) return true;

        // Status updates, cancellations, and marking payment on orders
        if (path.startsWith("/api/orders/")
                && (path.endsWith("/status") || path.endsWith("/cancel") || path.endsWith("/payments/mark-paid")))
            return true;

        return false;
    }
}
