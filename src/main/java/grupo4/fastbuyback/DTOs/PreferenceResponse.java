package grupo4.fastbuyback.DTOs;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PreferenceResponse(
        String preferenceId,
        String initPoint,
        String sandboxInitPoint,
        Boolean simulated
) {
    public static PreferenceResponse simulated(String initPoint) {
        return new PreferenceResponse(null, initPoint, null, true);
    }

    public static PreferenceResponse real(String preferenceId, String initPoint, String sandboxInitPoint) {
        return new PreferenceResponse(preferenceId, initPoint, sandboxInitPoint, false);
    }
}
