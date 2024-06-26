package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "security.halo.run", version = "v1alpha1", kind = "RememberMeToken", plural =
    "remembermetokens", singular = "remembermetoken")
public class RememberMeToken extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private Spec spec;

    @Data
    @Accessors(chain = true)
    @Schema(name = "RememberMeTokenSpec")
    public static class Spec {
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String username;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String series;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String tokenValue;

        private Instant lastUsed;
    }
}
