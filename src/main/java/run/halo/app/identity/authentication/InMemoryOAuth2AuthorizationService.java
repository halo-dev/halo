package run.halo.app.identity.authentication;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.util.Assert;

/**
 * An {@link OAuth2AuthorizationService} that stores {@link OAuth2Authorization}'s in-memory.<p>
 * <b>NOTE:</b> This implementation should ONLY be used during development/testing.
 *
 * @author guqing
 * @see OAuth2AuthorizationService
 * @since 2.0.0
 */
public class InMemoryOAuth2AuthorizationService implements OAuth2AuthorizationService {
    private int maxInitializedAuthorizations = 100;

    /*
     * Stores "initialized" (uncompleted) authorizations, where an access token has not yet been
     * granted.
     * This state occurs with the authorization_code grant flow during the user consent step OR
     * when the code is returned in the authorization response but the access token request is
     * not yet initiated.
     */
    private Map<String, OAuth2Authorization> initializedAuthorizations =
        Collections.synchronizedMap(new MaxSizeHashMap<>(this.maxInitializedAuthorizations));

    /*
     * Stores "completed" authorizations, where an access token has been granted.
     */
    private final Map<String, OAuth2Authorization> authorizations = new ConcurrentHashMap<>();

    /*
     * Constructor used for testing only.
     */
    public InMemoryOAuth2AuthorizationService(int maxInitializedAuthorizations) {
        this.maxInitializedAuthorizations = maxInitializedAuthorizations;
        this.initializedAuthorizations =
            Collections.synchronizedMap(new MaxSizeHashMap<>(this.maxInitializedAuthorizations));
    }

    /**
     * Constructs an {@code InMemoryOAuth2AuthorizationService}.
     */
    public InMemoryOAuth2AuthorizationService() {
        this(Collections.emptyList());
    }

    /**
     * Constructs an {@code InMemoryOAuth2AuthorizationService} using the provided parameters.
     *
     * @param authorizations the authorization(s)
     */
    public InMemoryOAuth2AuthorizationService(OAuth2Authorization... authorizations) {
        this(Arrays.asList(authorizations));
    }

    /**
     * Constructs an {@code InMemoryOAuth2AuthorizationService} using the provided parameters.
     *
     * @param authorizations the authorization(s)
     */
    public InMemoryOAuth2AuthorizationService(List<OAuth2Authorization> authorizations) {
        Assert.notNull(authorizations, "authorizations cannot be null");
        authorizations.forEach(authorization -> {
            Assert.notNull(authorization, "authorization cannot be null");
            Assert.isTrue(!this.authorizations.containsKey(authorization.getId()),
                "The authorization must be unique. Found duplicate identifier: "
                    + authorization.getId());
            this.authorizations.put(authorization.getId(), authorization);
        });
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        if (isComplete(authorization)) {
            this.authorizations.put(authorization.getId(), authorization);
        } else {
            this.initializedAuthorizations.put(authorization.getId(), authorization);
        }
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        if (isComplete(authorization)) {
            this.authorizations.remove(authorization.getId(), authorization);
        } else {
            this.initializedAuthorizations.remove(authorization.getId(), authorization);
        }
    }

    @Nullable
    @Override
    public OAuth2Authorization findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        OAuth2Authorization authorization = this.authorizations.get(id);
        return authorization != null
            ? authorization :
            this.initializedAuthorizations.get(id);
    }

    @Nullable
    @Override
    public OAuth2Authorization findByToken(String token, @Nullable OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");
        for (OAuth2Authorization authorization : this.authorizations.values()) {
            if (hasToken(authorization, token, tokenType)) {
                return authorization;
            }
        }
        for (OAuth2Authorization authorization : this.initializedAuthorizations.values()) {
            if (hasToken(authorization, token, tokenType)) {
                return authorization;
            }
        }
        return null;
    }

    private static boolean isComplete(OAuth2Authorization authorization) {
        return authorization.getAccessToken() != null;
    }

    private static boolean hasToken(OAuth2Authorization authorization, String token,
        @Nullable OAuth2TokenType tokenType) {
        if (tokenType == null) {
            return matchesAccessToken(authorization, token)
                || matchesRefreshToken(authorization, token);
        } else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            return matchesAccessToken(authorization, token);
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            return matchesRefreshToken(authorization, token);
        }
        return false;
    }

    private static boolean matchesAccessToken(OAuth2Authorization authorization, String token) {
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken =
            authorization.getToken(OAuth2AccessToken.class);
        return accessToken != null && accessToken.getToken().getTokenValue().equals(token);
    }

    private static boolean matchesRefreshToken(OAuth2Authorization authorization, String token) {
        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
            authorization.getToken(OAuth2RefreshToken.class);
        return refreshToken != null && refreshToken.getToken().getTokenValue().equals(token);
    }

    private static final class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {
        private final int maxSize;

        private MaxSizeHashMap(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > this.maxSize;
        }
    }
}
