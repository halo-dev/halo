package run.halo.app.identity.apitoken;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.util.Assert;

/**
 * <p>An implementation of {@link OAuth2TokenValidator} for verifying personal access token.</p>
 * <p>Because clocks can differ between the personal-access-token source, say the Authorization
 * Server, and its destination, say the Resource Server, there is a default clock leeway
 * exercised when deciding if the current time is within the {@link PersonalAccessToken}'s
 * specified operating window.</p>
 *
 * @author guqing
 * @see OAuth2TokenValidator
 * @see PersonalAccessToken
 * @since 2.0.0
 */
@Slf4j
public class PersonalAccessTokenTimestampValidator implements
    OAuth2TokenValidator<PersonalAccessToken> {

    private static final Duration DEFAULT_MAX_CLOCK_SKEW = Duration.of(60, ChronoUnit.SECONDS);

    private final Duration clockSkew;

    private Clock clock = Clock.systemUTC();

    /**
     * A basic instance with no custom verification and the default max clock skew.
     */
    public PersonalAccessTokenTimestampValidator() {
        this(DEFAULT_MAX_CLOCK_SKEW);
    }

    public PersonalAccessTokenTimestampValidator(Duration clockSkew) {
        Assert.notNull(clockSkew, "clockSkew cannot be null");
        this.clockSkew = clockSkew;
    }

    @Override
    public OAuth2TokenValidatorResult validate(PersonalAccessToken token) {
        Assert.notNull(token, "personalAccessToken cannot be null");
        Instant expiry = token.getExpiresAt();
        if (expiry != null) {
            if (Instant.now(this.clock).minus(this.clockSkew).isAfter(expiry)) {
                OAuth2Error oauth2Error =
                    createOauth2Error(
                        String.format("personal-access-token expired at %s", token.getExpiresAt()));
                return OAuth2TokenValidatorResult.failure(oauth2Error);
            }
        }
        Instant notBefore = token.getIssuedAt();
        if (notBefore != null) {
            if (Instant.now(this.clock).plus(this.clockSkew).isBefore(notBefore)) {
                OAuth2Error oauth2Error = createOauth2Error(
                    String.format("personal-access-token used before %s", token.getIssuedAt()));
                return OAuth2TokenValidatorResult.failure(oauth2Error);
            }
        }
        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2Error createOauth2Error(String reason) {
        log.debug(reason);
        return new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, reason,
            "https://github.com/halo-dev/rfcs/blob/main/identity/003-encryption.md");
    }

    /**
     * Use this {@link Clock} with {@link Instant#now()} for assessing timestamp validity.
     *
     * @param clock A clock providing access to the current instant
     */
    public void setClock(Clock clock) {
        Assert.notNull(clock, "clock cannot be null");
        this.clock = clock;
    }
}
