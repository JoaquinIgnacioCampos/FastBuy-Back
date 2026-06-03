package grupo4.fastbuyback.Services;

import grupo4.fastbuyback.DTOs.LoginRequest;
import grupo4.fastbuyback.DTOs.LoginResponse;
import grupo4.fastbuyback.Entities.Bar;
import grupo4.fastbuyback.Entities.BartenderUser;
import grupo4.fastbuyback.Repositories.BartenderUsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final BartenderUsersRepository repo;
    private final PasswordEncoder encoder;

    public AuthService(BartenderUsersRepository repo, PasswordEncoder encoder) {
        this.repo    = repo;
        this.encoder = encoder;
    }

    public LoginResponse loginBartender(LoginRequest req) {
        BartenderUser user = repo.findByUsername(req.username())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        Bar bar = user.getBar();
        return new LoginResponse(
            user.getUsername(),
            bar.getId(),
            bar.getLabel(),
            bar.getEventId(),
            bar.getEvent().getName()
        );
    }
}
