package run.halo.app.identity.authentication;

import java.util.Map;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link OAuth2TokenContext}.
 *
 * @author guqing
 * @since 2.0.0
 */
public record DefaultOAuth2TokenContext(Map<Object, Object> context) implements OAuth2TokenContext {
    public DefaultOAuth2TokenContext {
        context = Map.copyOf(context);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <V> V get(Object key) {
        return hasKey(key) ? (V) this.context.get(key) : null;
    }

    @Override
    public boolean hasKey(Object key) {
        Assert.notNull(key, "key cannot be null");
        return this.context.containsKey(key);
    }

    /**
     * Returns a new {@link Builder}.
     *
     * @return the {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for {@link DefaultOAuth2TokenContext}.
     */
    public static final class Builder extends AbstractBuilder<DefaultOAuth2TokenContext, Builder> {

        private Builder() {
        }

        /**
         * Builds a new {@link DefaultOAuth2TokenContext}.
         *
         * @return the {@link DefaultOAuth2TokenContext}
         */
        public DefaultOAuth2TokenContext build() {
            return new DefaultOAuth2TokenContext(getContext());
        }
    }
}
