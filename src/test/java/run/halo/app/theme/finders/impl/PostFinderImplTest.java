package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentService;
import run.halo.app.content.ContentWrapper;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.ContributorFinder;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.ContentVo;

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