package run.halo.app.theme.finders.vo;

import lombok.Builder;
import lombok.Data;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;

/**
 * Comment owner vo.
 *
 * @author LIlGG
 */
@Data
@Builder
public class CommentOwnerVo {
    String kind;

    String displayName;

    String avatar;

    /**
     * Convert user to owner info by owner that has an email kind .
     *
     * @param owner comment owner reference.
     * @return owner info.
     */
    public static CommentOwnerVo from(Comment.CommentOwner owner) {
        if (!Comment.CommentOwner.KIND_EMAIL.equals(owner.getKind())) {
            throw new IllegalArgumentException("Only support 'email' owner kind.");
        }
        return CommentOwnerVo.builder()
            .kind(owner.getKind())
            .displayName(owner.getDisplayName())
            .avatar(owner.getAnnotation(Comment.CommentOwner.AVATAR_ANNO))
            .build();
    }

    /**
     * Convert user to comment owner vo by {@link User}.
     *
     * @param user user extension.
     * @return comment owner vo
     */
    public static CommentOwnerVo from(User user) {
        return CommentOwnerVo.builder()
            .kind(user.getKind())
            .displayName(user.getSpec().getDisplayName())
            .avatar(user.getSpec().getAvatar())
            .build();
    }
}
