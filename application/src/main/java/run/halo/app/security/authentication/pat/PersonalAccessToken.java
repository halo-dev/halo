package run.halo.app.security.authentication.pat;

import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@GVK(group = "",
    version = "v1alpha1",
    kind = "PersonalAccessToken",
    singular = "personalaccesstoken",
    plural = "personalaccesstokens")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PersonalAccessToken extends AbstractExtension {

    private PersonalAccessTokenSpec spec;

    @Data
    public static class PersonalAccessTokenSpec {

        private String userName;

        private String displayName;

        private Boolean revoked;

        private Instant expiresAt;

        private String scopes;

        private String tokenDigest;

    }

}
