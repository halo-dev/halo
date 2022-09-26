package run.halo.app.theme.finders.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.Assert;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.MetadataOperator;

/**
 * A value object for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
@ToString
@EqualsAndHashCode
public class PostVo {

    private MetadataOperator metadata;

    private Post.PostSpec spec;

    private Post.PostStatus status;

    private ContentVo content;

    private List<CategoryVo> categories;

    private List<TagVo> tags;

    private List<Contributor> contributors;

    private StatsVo stats;

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
}
