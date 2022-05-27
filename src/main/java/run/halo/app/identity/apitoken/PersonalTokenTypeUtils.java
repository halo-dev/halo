package run.halo.app.identity.apitoken;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * An util for Personal access token type.
 *
 * @author guqing
 * @since 2.0.0
 */
public class PersonalTokenTypeUtils {
    private static final String TOKEN_TYPE_SEPARATOR = "_";

    /**
     * Remove the type prefix in the personal access token string.
     *
     * @param tokenValue personal access token
     * @return token removed prefix
     */
    public static String removeTypePrefix(String tokenValue) {
        String adminType = PersonalAccessTokenType.ADMIN_TOKEN.value() + TOKEN_TYPE_SEPARATOR;
        if (StringUtils.startsWith(tokenValue, adminType)) {
            return StringUtils.substringAfter(tokenValue, adminType);
        }
        String contentType = PersonalAccessTokenType.CONTENT_TOKEN.value() + TOKEN_TYPE_SEPARATOR;
        if (StringUtils.startsWith(tokenValue, contentType)) {
            return StringUtils.substringAfter(tokenValue, contentType);
        }
        return tokenValue;
    }

    /**
     * Judge whether it is a personal access token of admin type.
     *
     * @param tokenValue personal access token
     * @return {@code true} if it is a token of admin type, otherwise {@code false}
     */
    public static boolean isAdminToken(String tokenValue) {
        Assert.notNull(tokenValue, "The tokenValue must not be null.");
        String adminType = PersonalAccessTokenType.ADMIN_TOKEN.value() + TOKEN_TYPE_SEPARATOR;
        return StringUtils.startsWith(tokenValue, adminType);
    }

    /**
     * Judge whether it is a personal access token of content type.
     *
     * @param tokenValue personal access token
     * @return {@code true} if it is a token of content type, otherwise {@code false}
     */
    public static boolean isContentToken(String tokenValue) {
        String contentType = PersonalAccessTokenType.CONTENT_TOKEN.value() + TOKEN_TYPE_SEPARATOR;
        return StringUtils.startsWith(tokenValue, contentType);
    }

    /**
     * Determine whether there is a personal access token type prefix.
     *
     * @param tokenValue personal access token
     * @return {@code true} if it is starts with {@link PersonalAccessTokenType#ADMIN_TOKEN} or
     * {@link PersonalAccessTokenType#CONTENT_TOKEN} prefix, otherwise {@code false}
     */
    public static boolean isPersonalAccessToken(String tokenValue) {
        return isAdminToken(tokenValue) || isContentToken(tokenValue);
    }
}
