package br.com.serene;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Set;
import java.net.URI;
import org.springframework.web.filter.OncePerRequestFilter;
public class OriginGuardFilter extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        boolean browserWriteAllowed = origin != null && sameOrigin(origin, request)
            && "admin-web".equals(request.getHeader("X-Serene-Client"));
        if (!Set.of("GET", "HEAD", "OPTIONS").contains(request.getMethod()) &&
            ((origin != null && !browserWriteAllowed) || "cross-site".equals(request.getHeader("Sec-Fetch-Site")))) {
            response.setStatus(403); response.setContentType("application/json");
            response.getWriter().write("{\"status\":403,\"message\":\"Origem da requisição não permitida.\",\"errors\":{}}"); return;
        }
        chain.doFilter(request, response);
    }
    private boolean sameOrigin(String origin, HttpServletRequest request) {
        try {
            URI uri = URI.create(origin);
            int port = uri.getPort() == -1 ? ("https".equals(uri.getScheme()) ? 443 : 80) : uri.getPort();
            return uri.getUserInfo() == null && uri.getQuery() == null && uri.getFragment() == null
                && (uri.getRawPath() == null || uri.getRawPath().isEmpty())
                && request.getScheme().equals(uri.getScheme()) && request.getServerName().equalsIgnoreCase(uri.getHost())
                && request.getServerPort() == port;
        } catch (IllegalArgumentException ex) { return false; }
    }
}
