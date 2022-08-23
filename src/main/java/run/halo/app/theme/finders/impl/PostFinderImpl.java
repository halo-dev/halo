package run.halo.app.theme.finders.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentService;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.SubscriberUtils;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.PostVo;

/**
 * A finder for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("postFinder")
public class PostFinderImpl implements PostFinder {
    private static final Comparator<Post> DEFAULT_ORDER = (o1, o2) -> {
        Assert.notNull(o1, "o1 must not be null");
        Assert.notNull(o2, "o2 must not be null");
        if (o1 == o2) {
            return 0;
        }
        if (Objects.equals(true, o1.getSpec().getPinned())) {
            if (Objects.equals(true, o2.getSpec().getPinned())) {
                return o1.getSpec().getPriority().compareTo(o2.getSpec().getPriority());
            }
            return 1;
        }

        int compare = o1.getMetadata().getCreationTimestamp()
            .compareTo(o2.getMetadata().getCreationTimestamp());
        return compare == 0 ? o1.getMetadata().getName()
            .compareTo(o2.getMetadata().getName()) : compare;
    };

    private final ReactiveExtensionClient client;

    private final ContentService contentService;

    public PostFinderImpl(ReactiveExtensionClient client, ContentService contentService) {
        this.client = client;
        this.contentService = contentService;
    }

    @Override
    public ContentVo content(String postName) {
        Mono<ContentVo> mono = contentService.getContent(postName)
            .map(wrapper -> ContentVo.builder().content(wrapper.content())
                .raw(wrapper.raw()).build());
        return SubscriberUtils.subscribe(mono);
    }

    @Override
    public ListResult<PostVo> list(int page, int size) {
        ListResult<Post> posts = listPost(page, size, null);
        List<PostVo> postVos = posts.get().map(PostVo::from)
            .collect(Collectors.toList());
        return new ListResult<>(posts.getPage(), posts.getSize(), posts.getTotal(), postVos);
    }

    @Override
    public ListResult<PostVo> list(int page, int size, String categoryName) {
        ListResult<Post> posts = listPost(page, size,
            post -> contains(post.getSpec().getCategories(), categoryName));
        List<PostVo> postVos = posts.get().map(PostVo::from)
            .collect(Collectors.toList());
        return new ListResult<>(posts.getPage(), posts.getSize(), posts.getTotal(), postVos);
    }

    private boolean contains(List<String> c, String key) {
        if (StringUtils.isBlank(key) || c == null) {
            return false;
        }
        return c.contains(key);
    }

    private ListResult<Post> listPost(int page, int size, Predicate<Post> postPredicate) {
        Predicate<Post> fixedPredicate = post ->
            Objects.equals(false, post.getSpec().getDeleted())
                && Objects.equals(true, post.getSpec().getPublished())
                && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible());
        Predicate<Post> predicate = fixedPredicate
            .and(postPredicate == null ? post -> true : postPredicate);
        Mono<ListResult<Post>> mono = client.list(Post.class, predicate,
            DEFAULT_ORDER.reversed(), Math.max(page - 1, 0), size);
        return SubscriberUtils.subscribe(mono);
    }
}
