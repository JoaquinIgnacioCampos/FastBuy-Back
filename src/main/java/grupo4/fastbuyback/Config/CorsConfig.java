package grupo4.fastbuyback.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Allowed browser origins, comma-separated. Defaults to the local Vite dev
     * server; set FASTBUY_CORS_ORIGINS in production to the deployed frontend
     * origin(s), e.g. https://fastbuy.pages.dev
     */
    @Value("${fastbuy.cors-origins:http://localhost:5173}")
    private String[] origins;

    private final BartenderAuthInterceptor bartenderAuth;

    public CorsConfig(BartenderAuthInterceptor bartenderAuth) {
        this.bartenderAuth = bartenderAuth;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(origins)
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
