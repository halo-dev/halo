package run.halo.app.theme.finders.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.content.ContentService;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.CategoryVo;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.PostVo;
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

    public PostFinderImpl(ReactiveExtensionClient client,
        ContentService contentService,
        TagFinder tagFinder,
        CategoryFinder categoryFinder) {
        this.client = client;
        this.contentService = contentService;
        this.tagFinder = tagFinder;
        this.categoryFinder = categoryFinder;
    }

    @Override
    public ContentVo content(String postName) {
        return contentService.getContent(postName)
            .map(wrapper -> ContentVo.builder().content(wrapper.content())
                .raw(wrapper.raw()).build())
            .block();
    }

    @Override
    public ListResult<PostVo> list(int page, int size) {
        return listPost(page, size, null);
    }

    @Override
    public ListResult<PostVo> listByCategory(int page, int size, String categoryName) {
        return listPost(page, size,
            post -> contains(post.getSpec().getCategories(), categoryName));
    }

    @Override
    public ListResult<PostVo> listByTag(int page, int size, String tag) {
        return listPost(page, size,
            post -> contains(post.getSpec().getTags(), tag));
    }

    private boolean contains(List<String> c, String key) {
        if (StringUtils.isBlank(key) || c == null) {
            return false;
        }
        return c.contains(key);
    }

    private ListResult<PostVo> listPost(int page, int size, Predicate<Post> postPredicate) {
        Predicate<Post> predicate = FIXED_PREDICATE
            .and(postPredicate == null ? post -> true : postPredicate);
        ListResult<Post> list = client.list(Post.class, predicate,
                defaultComparator(), Math.max(page - 1, 0), size)
            .block();
        if (list == null) {
            return new ListResult<>(0, 0, 0, List.of());
        }
        List<PostVo> postVos = list.get()
            .map(post -> {
                PostVo postVo = PostVo.from(post);
                List<CategoryVo> categoryVos =
                    categoryFinder.getByNames(post.getSpec().getCategories());
                List<TagVo> tagVos = tagFinder.getByNames(post.getSpec().getTags());
                return postVo.withTags(tagVos).withCategories(categoryVos);
            }).toList();
        return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(), postVos);
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
}
