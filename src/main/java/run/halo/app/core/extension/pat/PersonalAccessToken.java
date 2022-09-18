package run.halo.app.core.extension.pat;

import static run.halo.app.core.extension.pat.Constant.GROUP;
import static run.halo.app.core.extension.pat.Constant.VERSION;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@GVK(group = GROUP, version = VERSION, kind = "PersonalAccessToken",
    singular = "personalaccesstoken", plural = "personalaccesstokens")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PersonalAccessToken extends AbstractExtension {

    private PersonalAccessTokenSpec spec;

    @Data
    public static class PersonalAccessTokenSpec {

        @Schema(description = "The user name of creating the token", required = true)
        private String createdBy;

        @Schema(description = "The description about the token", required = true)
        private String description;

        @Schema(description = "Indicates if the token is revoked", defaultValue = "false")
        private boolean revoked;

        @Schema(description = "Expiration time", nullable = true)
        private Instant expiresAt;

        @Schema(description = "Scopes define the access for personal tokens."
            + " If the scopes is empty, it means that the token only has public access",
            nullable = true)
        private Set<String> scopes;

        @Schema(description = "Encoded token is encoded by BCrypt")
        private String encodedToken;

    }

}
