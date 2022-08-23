package run.halo.app.theme.finders.vo;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import org.springframework.util.Assert;
import run.halo.app.core.extension.Post;

/**
 * A value object for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
public class PostVo {

    String name;

    String title;

    String slug;

    String snapshot;

    String owner;

    String template;

    String cover;

    Boolean published;

    Instant publishTime;

    Boolean pinned;

    Boolean allowComment;

    Post.VisibleEnum visible;

    Integer version;

    Integer priority;

    String excerpt;

    List<String> categories;

    List<String> tags;

    List<Map<String, String>> htmlMetas;

    String permalink;

    List<String> contributors;

    Map<String, String> annotations;

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
            .categories(spec.getCategories())
            .tags(spec.getTags())
            .owner(spec.getOwner())
            .pinned(spec.getPinned())
            .slug(spec.getSlug())
            .htmlMetas(spec.getHtmlMetas())
            .published(spec.getPublished())
            .priority(spec.getPriority())
            .version(spec.getVersion())
            .visible(spec.getVisible())
            .template(spec.getTemplate())
            .snapshot(spec.getReleaseSnapshot())
            .permalink(postStatus.getPermalink())
            .excerpt(postStatus.getExcerpt())
            .contributors(postStatus.getContributors())
            .build();
    }
}
