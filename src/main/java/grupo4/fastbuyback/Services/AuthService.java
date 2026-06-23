package grupo4.fastbuyback.Services;

import grupo4.fastbuyback.DTOs.AdminLoginResponse;
import grupo4.fastbuyback.DTOs.LoginRequest;
import grupo4.fastbuyback.DTOs.LoginResponse;
import grupo4.fastbuyback.Entities.AdminUser;
import grupo4.fastbuyback.Entities.Bar;
import grupo4.fastbuyback.Entities.BartenderUser;
import grupo4.fastbuyback.Repositories.AdminUsersRepository;
import grupo4.fastbuyback.Repositories.BartenderUsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final BartenderUsersRepository bartenderRepo;
    private final AdminUsersRepository adminRepo;
    private final PasswordEncoder encoder;

    public AuthService(BartenderUsersRepository bartenderRepo,
                       AdminUsersRepository adminRepo,
                       PasswordEncoder encoder) {
        this.bartenderRepo = bartenderRepo;
        this.adminRepo     = adminRepo;
        this.encoder       = encoder;
    }

    public LoginResponse loginBartender(LoginRequest req) {
        BartenderUser user = bartenderRepo.findByUsername(req.username())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = UUID.randomUUID().toString();
        user.setSessionToken(token);
        bartenderRepo.save(user);

        Bar bar = user.getBar();
        return new LoginResponse(
            user.getUsername(),
            bar.getId(),
            bar.getLabel(),
            bar.getEventId(),
            bar.getEvent().getName(),
            token
        );
    }

    public AdminLoginResponse loginAdmin(LoginRequest req) {
        AdminUser user = adminRepo.findByUsername(req.username())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = UUID.randomUUID().toString();
        user.setSessionToken(token);
        adminRepo.save(user);

        return new AdminLoginResponse(
            user.getUsername(),
            user.getEvent().getId(),
            user.getEvent().getName(),
            token
        );
    }
}
