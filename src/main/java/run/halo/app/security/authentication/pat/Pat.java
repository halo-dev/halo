package run.halo.app.security.authentication.pat;

import java.time.Instant;
import org.springframework.lang.Nullable;

public interface Pat {

    String getTokenValue();

    String getPatName();

    @Nullable
    Instant getIssuedAt();

    @Nullable
    Instant getExpiresAt();

}
