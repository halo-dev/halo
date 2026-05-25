package run.halo.app.security;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/** Personal access token extension used for non-interactive API authentication on behalf of a Halo user. */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(
        group = "security.halo.run",
        version = "v1alpha1",
        kind = PersonalAccessToken.KIND,
        plural = "personalaccesstokens",
        singular = "personalaccesstoken")
public class PersonalAccessToken extends AbstractExtension {

    public static final String KIND = "PersonalAccessToken";

    public static final String PAT_TOKEN_PREFIX = "pat_";

    /** Desired token metadata, ownership, access grants, and lifecycle state. */
    private Spec spec = new Spec();

    /** Desired personal access token settings and lifecycle flags. */
    @Data
    @Schema(name = "PatSpec")
    public static class Spec {

        /** Display name chosen by the token owner. */
        @Schema(requiredMode = REQUIRED)
        private String name;

        /** Optional human-readable description of how the token is used. */
        private String description;

        /** Time when the token expires. A null value means the token does not expire by time. */
        private Instant expiresAt;

        /** Role names granted to this token. */
        private List<String> roles;

        /** Scope strings granted to this token. */
        private List<String> scopes;

        /** User metadata.name that owns this token. */
        @Schema(requiredMode = REQUIRED)
        private String username;

        /** Whether the token has been manually revoked. */
        private boolean revoked;

        /** Time when the token was revoked. */
        private Instant revokesAt;

        /** Last time the token was used successfully. */
        private Instant lastUsed;

        /** Stable token identifier stored separately from the secret token value. */
        @Schema(requiredMode = REQUIRED)
        private String tokenId;
    }
}
