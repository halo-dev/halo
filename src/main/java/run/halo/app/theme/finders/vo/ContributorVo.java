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
public class ContributorVo {
    String name;

    String displayName;

    String avatar;

    String bio;

    String permalink;

    /**
     * Convert {@link User} to {@link ContributorVo}.
     *
     * @param user user extension
     * @return contributor value object
     */
    public static ContributorVo from(User user) {
        User.UserStatus status = user.getStatus();
        String permalink = (status == null ? "" : status.getPermalink());
        return builder().name(user.getMetadata().getName())
            .displayName(user.getSpec().getDisplayName())
            .avatar(user.getSpec().getAvatar())
            .bio(user.getSpec().getBio())
            .permalink(permalink)
            .build();
    }
}
