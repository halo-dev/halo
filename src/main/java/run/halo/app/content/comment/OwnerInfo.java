package run.halo.app.content.comment;

import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.core.extension.Comment;
import run.halo.app.core.extension.User;

/**
 * Comment owner info.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
public class OwnerInfo {

    String kind;

    String name;

    String displayName;

    String avatar;

    String email;

    /**
     * Convert user to owner info by owner that has an email kind .
     *
     * @param owner comment owner reference.
     * @return owner info.
     */
    public static OwnerInfo from(Comment.CommentOwner owner) {
        if (!Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind())) {
            throw new IllegalArgumentException("Only support 'email' owner kind.");
        }
        return OwnerInfo.builder()
            .kind(owner.getKind())
            .name(owner.getName())
            .email(owner.getName())
            .displayName(owner.getDisplayName())
            .avatar(owner.getAnnotation(Comment.CommentOwner.AVATAR_ANNO))
            .build();
    }

    /**
     * Convert user to owner info by {@link User}.
     *
     * @param user user extension.
     * @return owner info.
     */
    public static OwnerInfo from(User user) {
        return OwnerInfo.builder()
            .kind(user.getKind())
            .name(user.getMetadata().getName())
            .email(user.getSpec().getEmail())
            .avatar(user.getSpec().getAvatar())
            .displayName(user.getSpec().getDisplayName())
            .build();
    }

    /**
     * Obtain a ghost owner info when user not found.
     *
     * @return a ghost user if user not found.
     */
    public static OwnerInfo ghostUser() {
        return OwnerInfo.builder()
            .kind(User.KIND)
            .name("ghost")
            .email(StringUtils.EMPTY)
            .displayName("Ghost")
            .build();
    }
}