package run.halo.app.identity.apitoken;

import org.springframework.util.Assert;

/**
 * <p>Personal access token type.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
public record PersonalAccessTokenType(String value) {

    public static final PersonalAccessTokenType ADMIN_TOKEN = new PersonalAccessTokenType("ha");
    public static final PersonalAccessTokenType CONTENT_TOKEN = new PersonalAccessTokenType("hc");

    /**
     * Constructs an {@code PersonalAccessTokenType} using the provided value.
     *
     * @param value the value of the token type
     */
    public PersonalAccessTokenType {
        Assert.hasText(value, "value cannot be empty");
    }
}
