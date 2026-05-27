package grupo4.fastbuyback.Config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Maps service-layer exceptions to proper HTTP status codes so clients receive
 * structured JSON error responses instead of raw 500s.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Order/entity not found → 404 */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }

    /** Invalid state transition (e.g. advance READY, deliver non-READY) → 422 */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleInvalidState(IllegalStateException ex) {
        return Map.of("error", ex.getMessage());
    }
}
