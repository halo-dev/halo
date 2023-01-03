package run.halo.app.theme.finders.vo;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.MetadataOperator;

/**
 * A value object for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@SuperBuilder
@ToString
@EqualsAndHashCode
public class ListedPostVo implements ExtensionVoOperator {

    private MetadataOperator metadata;

    private Post.PostSpec spec;

    private Post.PostStatus status;

    private List<CategoryVo> categories;

    private List<TagVo> tags;

    private List<ContributorVo> contributors;

    private ContributorVo owner;

    private StatsVo stats;

    /**
     * Convert {@link Post} to {@link ListedPostVo}.
     *
     * @param post post extension
     * @return post value object
     */
    public static ListedPostVo from(Post post) {
        Assert.notNull(post, "The post must not be null.");
        Post.PostSpec spec = post.getSpec();
        Post.PostStatus postStatus = post.getStatusOrDefault();
        return ListedPostVo.builder()
            .metadata(post.getMetadata())
            .spec(spec)
            .status(postStatus)
            .categories(List.of())
            .tags(List.of())
            .contributors(List.of())
            .build();
    }
}
