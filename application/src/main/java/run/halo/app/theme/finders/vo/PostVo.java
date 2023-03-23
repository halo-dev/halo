package run.halo.app.theme.finders.vo;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.Post;

/**
 * A value object for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
public class PostVo extends ListedPostVo {

    private ContentVo content;

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
            .metadata(post.getMetadata())
            .spec(spec)
            .status(postStatus)
            .categories(List.of())
            .tags(List.of())
            .contributors(List.of())
            .content(new ContentVo(null, null))
            .build();
    }

    /**
     * Convert {@link Post} to {@link PostVo}.
     */
    public static PostVo from(ListedPostVo postVo) {
        return builder()
            .metadata(postVo.getMetadata())
            .spec(postVo.getSpec())
            .status(postVo.getStatus())
            .categories(postVo.getCategories())
            .tags(postVo.getTags())
            .contributors(postVo.getContributors())
            .owner(postVo.getOwner())
            .stats(postVo.getStats())
            .content(new ContentVo("", ""))
            .build();
    }
}
