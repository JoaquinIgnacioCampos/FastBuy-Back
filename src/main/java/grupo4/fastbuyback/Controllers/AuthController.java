package grupo4.fastbuyback.Controllers;

import grupo4.fastbuyback.DTOs.AdminLoginResponse;
import grupo4.fastbuyback.DTOs.LoginRequest;
import grupo4.fastbuyback.DTOs.LoginResponse;
import grupo4.fastbuyback.Services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/bartender/login")
    public ResponseEntity<LoginResponse> bartenderLogin(@Valid @RequestBody LoginRequest req) {
        try {
            return ResponseEntity.ok(service.loginBartender(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AdminLoginResponse> adminLogin(@Valid @RequestBody LoginRequest req) {
        try {
            return ResponseEntity.ok(service.loginAdmin(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
