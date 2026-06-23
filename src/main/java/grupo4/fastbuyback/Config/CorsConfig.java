package grupo4.fastbuyback.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final BartenderAuthInterceptor bartenderAuth;

    public CorsConfig(BartenderAuthInterceptor bartenderAuth) {
        this.bartenderAuth = bartenderAuth;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:5173",
                    "https://crepe-phonics-slogan.ngrok-free.dev"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Only bartender write actions are gated; GET reads and POST /orders stay open.
        registry.addInterceptor(bartenderAuth)
                .addPathPatterns(
                    "/orders/*/advance",
                    "/orders/*/deliver",
                    "/orders/*/cancel",
                    "/orders/*/release"
                );
    }
}
