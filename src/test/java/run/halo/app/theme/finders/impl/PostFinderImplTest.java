package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentService;
import run.halo.app.content.ContentWrapper;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.metrics.CounterService;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.ContributorFinder;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.PostArchiveVo;
import run.halo.app.theme.finders.vo.PostArchiveYearMonthVo;

/**
 * Tests for {@link PostFinderImpl}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PostFinderImplTest {

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private ContentService contentService;

    @Mock
    private CounterService counterService;

    @Mock
    private CategoryFinder categoryFinder;

    @Mock
    private TagFinder tagFinder;

    @Mock
    private ContributorFinder contributorFinder;

    @InjectMocks
    private PostFinderImpl postFinder;

    @Test
    void content() {
        Post post = post(1);
        post.getSpec().setReleaseSnapshot("release-snapshot");
        ContentWrapper contentWrapper = new ContentWrapper("snapshot", "raw", "content", "rawType");
        when(client.fetch(eq(Post.class), eq("post-1")))
            .thenReturn(Mono.just(post));
        when(contentService.getContent(post.getSpec().getReleaseSnapshot()))
            .thenReturn(Mono.just(contentWrapper));
        ContentVo content = postFinder.content("post-1");
        assertThat(content.getContent()).isEqualTo(contentWrapper.content());
        assertThat(content.getRaw()).isEqualTo(contentWrapper.raw());
    }

    @Test
    void compare() {
        List<String> strings = posts().stream().sorted(PostFinderImpl.defaultComparator())
            .map(post -> post.getMetadata().getName())
            .toList();
        assertThat(strings).isEqualTo(
            List.of("post-6", "post-2", "post-1", "post-5", "post-4", "post-3"));
    }

    @Test
    void predicate() {
        List<String> strings = posts().stream().filter(PostFinderImpl.FIXED_PREDICATE)
            .map(post -> post.getMetadata().getName())
            .toList();
        assertThat(strings).isEqualTo(List.of("post-1", "post-2", "post-6"));
    }

    @Test
    void archives() {
        Counter counter = new Counter();
        counter.setMetadata(new Metadata());
        when(counterService.getByName(any())).thenReturn(counter);
        ListResult<Post> listResult = new ListResult<>(1, 10, 3, postsForArchives());
        when(client.list(eq(Post.class), any(), any(), anyInt(), anyInt()))
            .thenReturn(Mono.just(listResult));
        ListResult<PostArchiveVo> archives = postFinder.archives(1, 10);
        List<PostArchiveVo> items = archives.getItems();

        assertThat(items.size()).isEqualTo(2);
        assertThat(items.get(0).getYear()).isEqualTo("2022");
        assertThat(items.get(0).getMonths().size()).isEqualTo(1);
        List<PostArchiveYearMonthVo> months = items.get(0).getMonths();
        assertThat(months.get(0).getMonth()).isEqualTo("12");
        assertThat(months.get(0).getPosts()).hasSize(2);

        assertThat(items.get(1).getYear()).isEqualTo("2021");
        assertThat(items.get(1).getMonths()).hasSize(1);
        assertThat(items.get(1).getMonths().get(0).getMonth()).isEqualTo("01");
    }

    @Test
    void fixedSizeSlidingWindow() {
        PostFinderImpl.FixedSizeSlidingWindow<Integer>
            window = new PostFinderImpl.FixedSizeSlidingWindow<>(3);

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            window.add(i);
            list.add(Strings.join(window.elements(), ','));
        }
        assertThat(list).isEqualTo(
            List.of("0", "0,1", "0,1,2", "1,2,3", "2,3,4", "3,4,5", "4,5,6", "5,6,7", "6,7,8",
                "7,8,9")
        );
    }

    @Test
    void postPreviousNextPair() {
        List<String> postNames = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            postNames.add("post-" + i);
        }

        // post-0, post-1, post-2
        Pair<String, String> previousNextPair =
            PostFinderImpl.postPreviousNextPair(postNames, "post-0");
        assertThat(previousNextPair.getLeft()).isNull();
        assertThat(previousNextPair.getRight()).isEqualTo("post-1");

        previousNextPair = PostFinderImpl.postPreviousNextPair(postNames, "post-1");
        assertThat(previousNextPair.getLeft()).isEqualTo("post-0");
        assertThat(previousNextPair.getRight()).isEqualTo("post-2");

        // post-1, post-2, post-3
        previousNextPair = PostFinderImpl.postPreviousNextPair(postNames, "post-2");
        assertThat(previousNextPair.getLeft()).isEqualTo("post-1");
        assertThat(previousNextPair.getRight()).isEqualTo("post-3");

        // post-7, post-8, post-9
        previousNextPair = PostFinderImpl.postPreviousNextPair(postNames, "post-8");
        assertThat(previousNextPair.getLeft()).isEqualTo("post-7");
        assertThat(previousNextPair.getRight()).isEqualTo("post-9");

        previousNextPair = PostFinderImpl.postPreviousNextPair(postNames, "post-9");
        assertThat(previousNextPair.getLeft()).isEqualTo("post-8");
        assertThat(previousNextPair.getRight()).isNull();
    }

    List<Post> postsForArchives() {
        Post post1 = post(1);
        post1.getSpec().setPublished(true);
        post1.getSpec().setPublishTime(Instant.parse("2021-01-01T00:00:00Z"));
        post1.getMetadata().setCreationTimestamp(Instant.now());

        Post post2 = post(2);
        post2.getSpec().setPublished(true);
        post2.getSpec().setPublishTime(Instant.parse("2022-12-01T00:00:00Z"));
        post2.getMetadata().setCreationTimestamp(Instant.now());

        Post post3 = post(3);
        post3.getSpec().setPublished(true);
        post3.getSpec().setPublishTime(Instant.parse("2022-12-03T00:00:00Z"));
        post3.getMetadata().setCreationTimestamp(Instant.now());
        return List.of(post1, post2, post3);
    }

    List<Post> posts() {
        // 置顶的排前面按 priority 排序
        // 再根据创建时间排序
        // 相同再根据名称排序
        // 6, 2, 1, 5, 4, 3
        Post post1 = post(1);
        post1.getSpec().setPinned(false);
        post1.getMetadata().setCreationTimestamp(Instant.now().plusSeconds(20));

        Post post2 = post(2);
        post2.getSpec().setPinned(true);
        post2.getSpec().setPriority(2);
        post2.getMetadata().setCreationTimestamp(Instant.now());

        Post post3 = post(3);
        post3.getSpec().setDeleted(true);
        post3.getMetadata().setCreationTimestamp(Instant.now());

        Post post4 = post(4);
        post4.getSpec().setVisible(Post.VisibleEnum.PRIVATE);
        post4.getMetadata().setCreationTimestamp(Instant.now());

        Post post5 = post(5);
        post5.getSpec().setPublished(false);
        post5.getMetadata().setCreationTimestamp(Instant.now());

        Post post6 = post(6);
        post6.getSpec().setPinned(true);
        post6.getSpec().setPriority(3);
        post6.getMetadata().setCreationTimestamp(Instant.now());

        return List.of(post1, post2, post3, post4, post5, post6);
    }

    Post post(int i) {
        final Post post = new Post();
        Metadata metadata = new Metadata();
        metadata.setName("post-" + i);
        metadata.setCreationTimestamp(Instant.now());
        metadata.setAnnotations(Map.of("K1", "V1"));
        post.setMetadata(metadata);

        Post.PostSpec postSpec = new Post.PostSpec();
        postSpec.setDeleted(false);
        postSpec.setAllowComment(true);
        postSpec.setPublishTime(Instant.now());
        postSpec.setPinned(false);
        postSpec.setPriority(0);
        postSpec.setPublished(true);
        postSpec.setVisible(Post.VisibleEnum.PUBLIC);
        postSpec.setTitle("title-" + i);
        postSpec.setSlug("slug-" + i);
        post.setSpec(postSpec);

        Post.PostStatus postStatus = new Post.PostStatus();
        postStatus.setPermalink("/post-" + i);
        postStatus.setContributors(List.of("contributor-1", "contributor-2"));
        postStatus.setExcerpt("hello world!");
        post.setStatus(postStatus);
        return post;
    }
}