package run.halo.app.theme.finders.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import run.halo.app.content.ContentService;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.ContributorFinder;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.CategoryVo;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.Contributor;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.finders.vo.StatsVo;
import run.halo.app.theme.finders.vo.TagVo;

/**
 * A finder for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("postFinder")
public class PostFinderImpl implements PostFinder {

    public static final Predicate<Post> FIXED_PREDICATE = post ->
        Objects.equals(false, post.getSpec().getDeleted())
            && Objects.equals(true, post.getSpec().getPublished())
            && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible());
    private final ReactiveExtensionClient client;

    private final ContentService contentService;

    private final TagFinder tagFinder;

    private final CategoryFinder categoryFinder;

    private final ContributorFinder contributorFinder;

    private final CounterService counterService;

    public PostFinderImpl(ReactiveExtensionClient client,
        ContentService contentService,
        TagFinder tagFinder,
        CategoryFinder categoryFinder,
        ContributorFinder contributorFinder, CounterService counterService) {
        this.client = client;
        this.contentService = contentService;
        this.tagFinder = tagFinder;
        this.categoryFinder = categoryFinder;
        this.contributorFinder = contributorFinder;
        this.counterService = counterService;
    }

    @Override
    public PostVo getByName(String postName) {
        Post post = client.fetch(Post.class, postName)
            .block();
        if (post == null) {
            return null;
        }
        PostVo postVo = getPostVo(post);
        postVo.setContent(content(postName));
        return postVo;
    }

    @Override
    public ContentVo content(String postName) {
        return client.fetch(Post.class, postName)
            .map(post -> post.getSpec().getReleaseSnapshot())
            .flatMap(contentService::getContent)
            .map(wrapper -> ContentVo.builder().content(wrapper.content())
                .raw(wrapper.raw()).build())
            .block();
    }

    @Override
    public ListResult<PostVo> list(Integer page, Integer size) {
        return listPost(page, size, null);
    }

    @Override
    public ListResult<PostVo> listByCategory(Integer page, Integer size, String categoryName) {
        return listPost(page, size,
            post -> contains(post.getSpec().getCategories(), categoryName));
    }

    @Override
    public ListResult<PostVo> listByTag(Integer page, Integer size, String tag) {
        return listPost(page, size,
            post -> contains(post.getSpec().getTags(), tag));
    }

    private boolean contains(List<String> c, String key) {
        if (StringUtils.isBlank(key) || c == null) {
            return false;
        }
        return c.contains(key);
    }

    private ListResult<PostVo> listPost(Integer page, Integer size, Predicate<Post> postPredicate) {
        Predicate<Post> predicate = FIXED_PREDICATE
            .and(postPredicate == null ? post -> true : postPredicate);
        ListResult<Post> list = client.list(Post.class, predicate,
                defaultComparator(), pageNullSafe(page), sizeNullSafe(size))
            .block();
        if (list == null) {
            return new ListResult<>(0, 0, 0, List.of());
        }
        List<PostVo> postVos = list.get()
            .map(this::getPostVo)
            .peek(this::populateStats)
            .toList();
        return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(), postVos);
    }

    private void populateStats(PostVo postVo) {
        Counter counter =
            counterService.getByName(MeterUtils.nameOf(Post.class, postVo.getMetadata()
                .getName()));
        StatsVo statsVo = StatsVo.builder()
            .visit(counter.getVisit())
            .upvote(counter.getUpvote())
            .comment(counter.getApprovedComment())
            .build();
        postVo.setStats(statsVo);
    }

    private PostVo getPostVo(@NonNull Post post) {
        List<TagVo> tags = tagFinder.getByNames(post.getSpec().getTags());
        List<CategoryVo> categoryVos = categoryFinder.getByNames(post.getSpec().getCategories());
        List<Contributor> contributors =
            contributorFinder.getContributors(post.getStatus().getContributors());
        PostVo postVo = PostVo.from(post);
        postVo.setCategories(categoryVos);
        postVo.setTags(tags);
        postVo.setContributors(contributors);
        populateStats(postVo);
        return postVo;
    }

    static Comparator<Post> defaultComparator() {
        Function<Post, Boolean> pinned =
            post -> Objects.requireNonNullElse(post.getSpec().getPinned(), false);
        Function<Post, Integer> priority =
            post -> Objects.requireNonNullElse(post.getSpec().getPriority(), 0);
        Function<Post, Instant> creationTimestamp =
            post -> post.getMetadata().getCreationTimestamp();
        Function<Post, String> name = post -> post.getMetadata().getName();
        return Comparator.comparing(pinned)
            .thenComparing(priority)
            .thenComparing(creationTimestamp)
            .thenComparing(name)
            .reversed();
    }

    int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }

    int sizeNullSafe(Integer size) {
        return ObjectUtils.defaultIfNull(size, 10);
    }
}
