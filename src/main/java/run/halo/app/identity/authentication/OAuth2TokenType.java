package run.halo.app.identity.authentication;

import java.io.Serializable;
import org.springframework.util.Assert;

/**
 * @author guqing
 * @since 2.0.0
 */
public record OAuth2TokenType(String value) implements Serializable {
    public static final OAuth2TokenType ACCESS_TOKEN = new OAuth2TokenType("access_token");
    public static final OAuth2TokenType REFRESH_TOKEN = new OAuth2TokenType("refresh_token");

    /**
     * Constructs an {@code OAuth2TokenType} using the provided value.
     *
     * @param value the value of the token type
     */
    public OAuth2TokenType {
        Assert.hasText(value, "value cannot be empty");
    }
}
