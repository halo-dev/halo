package run.halo.app.notification;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import run.halo.app.infra.AnonymousUserConst;

/**
 * Identity for user.
 *
 * @author guqing
 * @since 2.10.0
 */
public record UserIdentity(String name) {
    public static final String SEPARATOR = "#";

    /**
     * Create identity with username to identify a user.
     *
     * @param username username
     * @return identity
     */
    public static UserIdentity of(String username) {
        return new UserIdentity(username);
    }

    /**
     * <p>Create identity with email to identify a user,
     * the name will be {@code anonymousUser#email}.</p>
     * <p>An anonymous user can not be identified by username so we use email to identify it.</p>
     *
     * @param email email
     * @return identity
     */
    public static UserIdentity anonymousWithEmail(String email) {
        Assert.notNull(email, "Email must not be null");
        String name = AnonymousUserConst.PRINCIPAL + SEPARATOR + email;
        return of(name);
    }

    public boolean isAnonymous() {
        return name().startsWith(AnonymousUserConst.PRINCIPAL + SEPARATOR);
    }

    /**
     * Gets email if the identity is an anonymous user.
     *
     * @return email if the identity is an anonymous user, otherwise empty
     */
    public Optional<String> getEmail() {
        if (isAnonymous()) {
            return Optional.of(name().substring(name().indexOf(SEPARATOR) + 1))
                .filter(StringUtils::isNotBlank);
        }
        return Optional.empty();
    }
}
