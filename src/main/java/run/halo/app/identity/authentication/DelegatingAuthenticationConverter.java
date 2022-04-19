package run.halo.app.identity.authentication;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.Assert;

/**
 * An {@link AuthenticationConverter} that simply delegates to it's
 * internal {@code List} of {@link AuthenticationConverter}(s).
 * <p>
 * Each {@link AuthenticationConverter} is given a chance to
 * {@link AuthenticationConverter#convert(HttpServletRequest)}
 * with the first {@code non-null} {@link Authentication} being returned.
 *
 * @author guqing
 * @see AuthenticationConverter
 * @since 2.0.0
 */
public class DelegatingAuthenticationConverter implements AuthenticationConverter {
    private final List<AuthenticationConverter> converters;

    /**
     * Constructs a {@code DelegatingAuthenticationConverter} using the provided parameters.
     *
     * @param converters a {@code List} of {@link AuthenticationConverter}(s)
     */
    public DelegatingAuthenticationConverter(List<AuthenticationConverter> converters) {
        Assert.notEmpty(converters, "converters cannot be empty");
        this.converters = Collections.unmodifiableList(new LinkedList<>(converters));
    }

    @Nullable
    @Override
    public Authentication convert(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        for (AuthenticationConverter converter : this.converters) {
            Authentication authentication = converter.convert(request);
            if (authentication != null) {
                return authentication;
            }
        }
        return null;
    }
}
