package run.halo.app.localization;

import java.util.List;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.GroupVersionKind;

/**
 * Declares the public display fields of {@code Comment} resources.
 *
 * @since 2.28.0
 */
@Component
public class CommentLocalizationDescriptor implements ResourceLocalizationDescriptor {
    @Override
    public GroupVersionKind supports() {
        return GroupVersionKind.fromExtension(Comment.class);
    }

    @Override
    public List<LocalizableField> fields() {
        return List.of(new LocalizableField("content.html", "/spec/content", LocalizableField.Format.HTML));
    }
}
