package grupo4.fastbuyback.Config;

import grupo4.fastbuyback.Repositories.BartenderUsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Gates bartender write endpoints: requires a valid "Authorization: Bearer <token>"
 * matching a logged-in bartender's session token. Customer reads/order-creation are
 * not registered with this interceptor and stay open.
 */
@Component
public class BartenderAuthInterceptor implements HandlerInterceptor {

    private final BartenderUsersRepository users;

    public BartenderAuthInterceptor(BartenderUsersRepository users) {
        this.users = users;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Let CORS preflight through (it carries no Authorization header).
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
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
