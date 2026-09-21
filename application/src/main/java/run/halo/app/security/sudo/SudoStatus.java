package run.halo.app.security.sudo;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/**
 * Current sudo confirmation status of the session.
 *
 * @param active whether the session is inside the sudo window
 * @param expiresAt when the current sudo window ends, if active
 * @param methods methods the current user can use to confirm
 * @author johnniang
 * @since 2.27.0
 */
public record SudoStatus(
        @Schema(requiredMode = REQUIRED) boolean active,
        Instant expiresAt,
        @Schema(requiredMode = REQUIRED) List<SudoMethod> methods) {}
