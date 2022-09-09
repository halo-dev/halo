package run.halo.app.theme.finders.vo;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import run.halo.app.core.extension.User;

/**
 * A value object for {@link run.halo.app.core.extension.User}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@ToString
@Builder
public class Contributor {
    String name;

    String displayName;

    String avatar;

    String bio;

    /**
     * Convert {@link User} to {@link Contributor}.
     *
     * @param user user extension
     * @return contributor value object
     */
    public static Contributor from(User user) {
        return builder().name(user.getMetadata().getName())
            .displayName(user.getSpec().getDisplayName())
            .avatar(user.getSpec().getAvatar())
            .bio(user.getSpec().getBio())
            .build();
    }
}
