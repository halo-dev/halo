package run.halo.app.content.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.CategoryService;
import run.halo.app.content.Contributor;
import run.halo.app.content.PostQuery;
import run.halo.app.content.Stats;
import run.halo.app.core.counter.CounterService;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;

/** Tests for {@link PostServiceImpl} list assembly. */
@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    CounterService counterService;

    @Mock
    UserService userService;

    @Mock
    CategoryService categoryService;

    PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        postService = new PostServiceImpl(client, counterService, userService, categoryService);
    }

    @Test
    void shouldAssembleListedPostsWithBatchQueries() {
        var post1 = post("post-1", "user-1", List.of("tag-b", "tag-a"), List.of("category-b", "category-a"));
        post1.getStatusOrDefault().setContributors(List.of("user-2", "user-1", "user-2"));
        var post2 = post("post-2", "missing-user", List.of("tag-a"), List.of("category-a"));

        var pageRequest = PageRequestImpl.of(1, 20);
        var query = mock(PostQuery.class);
        when(query.toListOptions()).thenReturn(new ListOptions());
        when(query.toPageRequest()).thenReturn(pageRequest);
        when(client.listBy(eq(Post.class), any(ListOptions.class), eq(pageRequest)))
                .thenReturn(Mono.just(new ListResult<>(1, 20, 2, List.of(post1, post2))));
        when(counterService.getByNames(anyCollection()))
                .thenReturn(Flux.just(counter("posts.content.halo.run/post-1", 10, 1, 2, 1)));
        when(client.listAll(eq(Tag.class), any(ListOptions.class), eq(Sort.by("metadata.creationTimestamp"))))
                // The creation timestamp order deliberately differs from the post spec order.
                .thenReturn(Flux.just(tag("tag-a"), tag("tag-b")));
        when(client.listAll(eq(Category.class), any(ListOptions.class), eq(Sort.unsorted())))
                .thenReturn(Flux.just(category("category-a"), category("category-b")));
        when(userService.getUsersOrGhosts(anyList())).thenAnswer(invocation -> {
            List<String> names = invocation.getArgument(0);
            return Flux.fromIterable(names).map(PostServiceImplTest::userByName);
        });

        var result = postService.listPost(query).block();

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(2);

        var listedPost1 = result.getItems().get(0);
        assertThat(listedPost1.getPost()).isSameAs(post1);
        assertThat(listedPost1.getTags())
                .extracting(tag -> tag.getMetadata().getName())
                .containsExactly("tag-a", "tag-b");
        assertThat(listedPost1.getCategories())
                .extracting(category -> category.getMetadata().getName())
                .containsExactly("category-b", "category-a");
        assertThat(listedPost1.getContributors())
                .extracting(Contributor::getName)
                .containsExactly("user-2", "user-1", "user-2");
        assertThat(listedPost1.getOwner().getName()).isEqualTo("user-1");
        assertThat(listedPost1.getOwner().getDisplayName()).isEqualTo("User One");
        assertThat(listedPost1.getStats().getVisit()).isEqualTo(10);
        assertThat(listedPost1.getStats().getApprovedComment()).isEqualTo(1);

        var listedPost2 = result.getItems().get(1);
        assertThat(listedPost2.getTags())
                .extracting(tag -> tag.getMetadata().getName())
                .containsExactly("tag-a");
        assertThat(listedPost2.getCategories())
                .extracting(category -> category.getMetadata().getName())
                .containsExactly("category-a");
        assertThat(listedPost2.getContributors()).isEmpty();
        assertThat(listedPost2.getOwner().getName()).isEqualTo(UserService.GHOST_USER_NAME);
        assertThat(listedPost2.getStats()).isEqualTo(Stats.empty());

        verify(counterService, times(1)).getByNames(anyCollection());
        verify(counterService, never()).getByName(anyString());
        verify(client, times(1)).listAll(eq(Tag.class), any(ListOptions.class), any(Sort.class));
        verify(client, times(1)).listAll(eq(Category.class), any(ListOptions.class), any(Sort.class));
        verify(userService, times(1)).getUsersOrGhosts(anyList());
        verify(userService, never()).getUserOrGhost(anyString());
    }

    @Test
    void shouldNotQueryRelatedResourcesWhenPageIsEmpty() {
        var pageRequest = PageRequestImpl.of(1, 20);
        var query = mock(PostQuery.class);
        when(query.toListOptions()).thenReturn(new ListOptions());
        when(query.toPageRequest()).thenReturn(pageRequest);
        when(client.listBy(eq(Post.class), any(ListOptions.class), eq(pageRequest)))
                .thenReturn(Mono.just(ListResult.emptyResult()));

        var result = postService.listPost(query).block();

        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
        verify(counterService, never()).getByNames(anyCollection());
        verify(userService, never()).getUsersOrGhosts(anyList());
        verify(client, never()).listAll(any(), any(ListOptions.class), any(Sort.class));
    }

    static Post post(String name, String owner, List<String> tags, List<String> categories) {
        var post = new Post();
        var metadata = new Metadata();
        metadata.setName(name);
        post.setMetadata(metadata);
        var spec = new Post.PostSpec();
        spec.setOwner(owner);
        spec.setTags(tags);
        spec.setCategories(categories);
        post.setSpec(spec);
        return post;
    }

    static Tag tag(String name) {
        var tag = new Tag();
        var metadata = new Metadata();
        metadata.setName(name);
        tag.setMetadata(metadata);
        return tag;
    }

    static Category category(String name) {
        var category = new Category();
        var metadata = new Metadata();
        metadata.setName(name);
        category.setMetadata(metadata);
        return category;
    }

    static Counter counter(String name, int visit, int upvote, int totalComment, int approvedComment) {
        var counter = new Counter();
        var metadata = new Metadata();
        metadata.setName(name);
        counter.setMetadata(metadata);
        counter.setVisit(visit);
        counter.setUpvote(upvote);
        counter.setTotalComment(totalComment);
        counter.setApprovedComment(approvedComment);
        return counter;
    }

    static User userByName(String name) {
        if ("user-1".equals(name)) {
            return user("user-1", "User One");
        }
        if ("user-2".equals(name)) {
            return user("user-2", "User Two");
        }
        return user(UserService.GHOST_USER_NAME, "已删除用户");
    }

    static User user(String name, String displayName) {
        var user = new User();
        var metadata = new Metadata();
        metadata.setName(name);
        user.setMetadata(metadata);
        user.getSpec().setDisplayName(displayName);
        return user;
    }
}
