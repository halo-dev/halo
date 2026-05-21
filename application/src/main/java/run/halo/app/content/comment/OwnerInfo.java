package run.halo.app.content.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;

/**
 * Resolved comment owner summary for display.
 *
 * @author guqing
 * @since 2.0.0
 */
@Schema(description = "Resolved owner summary for a comment or reply.")
@Value
@Builder
public class OwnerInfo {

    @Schema(description = "Owner kind, such as User or email.")
    String kind;

    @Schema(description = "Owner name. For Halo users, this is the user metadata name.")
    String name;

    @Schema(description = "Owner display name.")
    String displayName;

    @Schema(description = "Owner avatar URL.")
    String avatar;

    @Schema(description = "Owner email address when available.")
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
}
