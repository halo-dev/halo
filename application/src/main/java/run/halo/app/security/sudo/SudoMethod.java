package run.halo.app.security.sudo;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A sudo confirmation method available to the current user.
 *
 * @param name method name, such as {@code totp} or {@code email}
 * @param sendable whether a one-time code must be sent first
 * @param maskedTarget optional masked destination shown in the UI
 * @author johnniang
 * @since 2.27.0
 */
public record SudoMethod(
        @Schema(requiredMode = REQUIRED) String name,
        @Schema(requiredMode = REQUIRED) boolean sendable,
        String maskedTarget) {}
