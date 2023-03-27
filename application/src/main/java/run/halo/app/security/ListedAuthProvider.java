package run.halo.app.security;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * A listed value object for {@link run.halo.app.core.extension.AuthProvider}.
 *
 * @author guqing
 * @since 2.4.0
 */
@Data
@Builder
public class ListedAuthProvider {
    @Schema(requiredMode = REQUIRED)
    String name;

    @Schema(requiredMode = REQUIRED)
    String displayName;

    String description;

    String logo;

    String website;

    String authenticationUrl;

    String helpPage;

    String bindingUrl;

    String unbindingUrl;

    Boolean isBound;

    Boolean enabled;

    Boolean supportsBinding;
    
    Boolean privileged;
}
