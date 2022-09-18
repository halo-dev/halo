package run.halo.app.security.authentication.pat;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

public record ScopeGrantedAuthority(String scope) implements GrantedAuthority {

    public static final String SCOPE_PREFIX = "SCOPE_";

    public ScopeGrantedAuthority {
        Assert.hasText(scope, "A granted authority(scope) textual representation is required");
        scope = StringUtils.prependIfMissing(scope, SCOPE_PREFIX);
    }

    @Override
    public String getAuthority() {
        return scope;
    }

}
