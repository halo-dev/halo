package run.halo.app.theme.finders.vo;

import java.util.Objects;
import lombok.Builder;
import lombok.Value;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Tag;
import run.halo.app.extension.MetadataOperator;

/**
 * A value object for {@link Tag}.
 */
@Value
@Builder
public class TagVo {

    MetadataOperator metadata;

    Tag.TagSpec spec;

    Tag.TagStatus status;

    /**
     * Convert {@link Tag} to {@link TagVo}.
     *
     * @param tag tag extension
     * @return tag value object
     */
    public static TagVo from(Tag tag) {
        Tag.TagSpec spec = tag.getSpec();
        Tag.TagStatus status = tag.getStatusOrDefault();
        return TagVo.builder()
            .metadata(tag.getMetadata())
            .spec(spec)
            .status(status)
            .build();
    }

    /**
     * Gets the number of posts under the current tag.
     *
     * @return the number of posts
     */
    public long postCount() {
        if (this.status == null || this.status.getPosts() == null) {
            return 0;
        }
        return this.status.getPosts()
            .stream()
            .filter(post -> Objects.equals(true, post.getPublished())
                && Post.VisibleEnum.PUBLIC.equals(post.getVisible()))
            .count();
    }
}
