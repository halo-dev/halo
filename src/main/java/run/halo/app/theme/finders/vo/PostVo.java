package run.halo.app.theme.finders.vo;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.util.Assert;
import run.halo.app.core.extension.Post;

/**
 * A value object for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PostVo extends BasePostVo {

    ContentVo content;

    List<CategoryVo> categories;

    List<TagVo> tags;

    /**
     * Convert {@link Post} to {@link PostVo}.
     *
     * @param post post extension
     * @return post value object
     */
    public static PostVo from(Post post) {
        Assert.notNull(post, "The post must not be null.");
        Post.PostSpec spec = post.getSpec();
        Post.PostStatus postStatus = post.getStatusOrDefault();
        return PostVo.builder()
            .name(post.getMetadata().getName())
            .annotations(post.getMetadata().getAnnotations())
            .title(spec.getTitle())
            .cover(spec.getCover())
            .allowComment(spec.getAllowComment())
            .categories(List.of())
            .tags(List.of())
            .owner(spec.getOwner())
            .pinned(spec.getPinned())
            .slug(spec.getSlug())
            .htmlMetas(nullSafe(spec.getHtmlMetas()))
            .published(spec.getPublished())
            .publishTime(spec.getPublishTime())
            .priority(spec.getPriority())
            .version(spec.getVersion())
            .visible(spec.getVisible())
            .template(spec.getTemplate())
            .permalink(postStatus.getPermalink())
            .excerpt(postStatus.getExcerpt())
            .contributors(List.of())
            .content(new ContentVo(null, null))
            .build();
    }
}
