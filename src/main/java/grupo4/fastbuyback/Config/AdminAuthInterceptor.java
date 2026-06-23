package grupo4.fastbuyback.Config;

import grupo4.fastbuyback.Repositories.AdminUsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Gates payment-account write endpoints (POST/DELETE): requires a valid
 * "Authorization: Bearer <token>" matching a logged-in admin's session token.
 * GET reads are allowed without authentication.
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AdminUsersRepository users;

    public AdminAuthInterceptor(AdminUsersRepository users) {
        this.users = users;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        // GET and CORS preflight are open.
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (!token.isBlank() && users.findBySessionToken(token).isPresent()) {
                return true;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
