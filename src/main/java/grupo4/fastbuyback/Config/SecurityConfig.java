package grupo4.fastbuyback.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Encrypts per-event Mercado Pago access tokens at rest (AES-GCM). Set
     * FASTBUY_TOKEN_KEY (and optionally FASTBUY_TOKEN_SALT, hex) in production;
     * the dev default is intentionally insecure.
     */
    @Bean
    public TextEncryptor tokenEncryptor(
            @Value("${fastbuy.token-key:dev-insecure-token-key}") String password,
            @Value("${fastbuy.token-salt:5c0744940b5c369b}") String salt) {
        return Encryptors.delux(password, salt);
    }
}
