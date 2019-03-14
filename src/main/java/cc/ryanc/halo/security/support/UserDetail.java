package cc.ryanc.halo.security.support;

import cc.ryanc.halo.exception.AuthenticationException;
import cc.ryanc.halo.model.entity.User;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.lang.NonNull;

/**
 * User detail.
 *
 * @author johnniang
 */
@ToString
@EqualsAndHashCode
public class UserDetail {

    private User user;

    /**
     * Gets user info.
     *
     * @return user info
     * @throws AuthenticationException throws if the user is null
     */
    @NonNull
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
