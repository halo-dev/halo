package run.halo.app.content.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import run.halo.app.core.counter.MeterUtils;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * End-to-end tests for the console post and single page list APIs, which resolve categories, tags, contributors, owners
 * and stats for every item.
 */
@SpringBootTest
@AutoConfigureWebTestClient
@WithMockUser(username = "fake-user", password = "fake-password", roles = "fake-super-role")
class ListedContentIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReactiveExtensionClient client;

    @MockitoBean
    RoleService roleService;

    String suffix;

    @BeforeEach
    void setUp() {
        suffix = UUID.randomUUID().toString().substring(0, 8);
        var rule = new Role.PolicyRule.Builder()
                .apiGroups("*")
                .resources("*")
                .verbs("*")
                .build();
        var role = new Role();
        role.setMetadata(new Metadata());
        role.getMetadata().setName("super-role");
        role.setRules(List.of(rule));
        when(roleService.listDependenciesFlux(anySet())).thenReturn(Flux.just(role));
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @Test
    void listPostsShouldResolveRelatedResourcesInBatch() {
        var tag1 = createTag("it-tag-1-" + suffix, "IT Tag 1");
        var tag2 = createTag("it-tag-2-" + suffix, "IT Tag 2");
        var category1 = createCategory("it-category-1-" + suffix, "IT Category 1");
        var category2 = createCategory("it-category-2-" + suffix, "IT Category 2");
        var owner = createUser("it-user-" + suffix, "IT User");
        var postName = "it-post-" + suffix;
        createPost(postName, owner.getMetadata().getName(), List.of(tag2, tag1), List.of(category2, category1));
        createCounter(MeterUtils.nameOf(Post.class, postName), 11, 2, 3, 2);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/apis/api.console.halo.run/v1alpha1/posts")
                        .queryParam("keyword", suffix)
                        .queryParam("page", 1)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.total")
                .isEqualTo(1)
                .jsonPath("$.items[0].post.metadata.name")
                .isEqualTo(postName)
                .jsonPath("$.items[0].tags[*].metadata.name")
                .value(names -> assertThat((List<String>) names)
                        .containsExactlyInAnyOrder(
                                tag1.getMetadata().getName(), tag2.getMetadata().getName()))
                .jsonPath("$.items[0].categories[*].metadata.name")
                .value(names -> assertThat((List<String>) names)
                        .containsExactly(
                                category2.getMetadata().getName(),
                                category1.getMetadata().getName()))
                .jsonPath("$.items[0].contributors[*].name")
                .value(names -> assertThat((List<String>) names)
                        .containsExactly(owner.getMetadata().getName(), UserService.GHOST_USER_NAME))
                .jsonPath("$.items[0].owner.name")
                .isEqualTo(owner.getMetadata().getName())
                .jsonPath("$.items[0].owner.displayName")
                .isEqualTo("IT User")
                .jsonPath("$.items[0].stats.visit")
                .isEqualTo(11)
                .jsonPath("$.items[0].stats.upvote")
                .isEqualTo(2)
                .jsonPath("$.items[0].stats.totalComment")
                .isEqualTo(3)
                .jsonPath("$.items[0].stats.approvedComment")
                .isEqualTo(2);
    }

    @Test
    void listPostsShouldFallbackToGhostUserAndEmptyStats() {
        var postName = "it-post-ghost-" + suffix;
        var post = new Post();
        var metadata = new Metadata();
        metadata.setName(postName);
        post.setMetadata(metadata);
        var spec = new Post.PostSpec();
        spec.setTitle("IT Ghost Post " + suffix);
        spec.setSlug(postName);
        spec.setOwner("missing-owner-" + suffix);
        spec.setDeleted(false);
        spec.setPublish(true);
        spec.setPinned(false);
        spec.setAllowComment(true);
        spec.setVisible(Post.VisibleEnum.PUBLIC);
        spec.setPriority(0);
        spec.setExcerpt(excerpt());
        post.setSpec(spec);
        client.create(post).block();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/apis/api.console.halo.run/v1alpha1/posts")
                        .queryParam("keyword", suffix)
                        .queryParam("page", 1)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.total")
                .isEqualTo(1)
                .jsonPath("$.items[0].owner.name")
                .isEqualTo(UserService.GHOST_USER_NAME)
                .jsonPath("$.items[0].stats.visit")
                .isEqualTo(0)
                .jsonPath("$.items[0].tags.length()")
                .isEqualTo(0)
                .jsonPath("$.items[0].categories.length()")
                .isEqualTo(0)
                .jsonPath("$.items[0].contributors.length()")
                .isEqualTo(0);
    }

    @Test
    void listSinglePagesShouldResolveRelatedResourcesInBatch() {
        var owner = createUser("it-page-user-" + suffix, "IT Page User");
        var pageName = "it-page-" + suffix;
        var page = new SinglePage();
        var metadata = new Metadata();
        metadata.setName(pageName);
        page.setMetadata(metadata);
        var spec = new SinglePage.SinglePageSpec();
        spec.setTitle("IT Page " + suffix);
        spec.setSlug(pageName);
        spec.setOwner(owner.getMetadata().getName());
        spec.setDeleted(false);
        spec.setPublish(true);
        spec.setPinned(false);
        spec.setAllowComment(true);
        spec.setVisible(Post.VisibleEnum.PUBLIC);
        spec.setPriority(0);
        spec.setExcerpt(excerpt());
        page.setSpec(spec);
        page.getStatusOrDefault().setContributors(List.of(owner.getMetadata().getName(), "missing-" + suffix));
        client.create(page).block();
        createCounter(MeterUtils.nameOf(SinglePage.class, pageName), 5, 1, 4, 3);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/apis/api.console.halo.run/v1alpha1/singlepages")
                        .queryParam("keyword", suffix)
                        .queryParam("page", 1)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.total")
                .isEqualTo(1)
                .jsonPath("$.items[0].page.metadata.name")
                .isEqualTo(pageName)
                .jsonPath("$.items[0].contributors[*].name")
                .value(names -> assertThat((List<String>) names)
                        .containsExactly(owner.getMetadata().getName(), UserService.GHOST_USER_NAME))
                .jsonPath("$.items[0].owner.name")
                .isEqualTo(owner.getMetadata().getName())
                .jsonPath("$.items[0].stats.visit")
                .isEqualTo(5)
                .jsonPath("$.items[0].stats.totalComment")
                .isEqualTo(4)
                .jsonPath("$.items[0].stats.approvedComment")
                .isEqualTo(3);
    }

    Tag createTag(String name, String displayName) {
        var tag = new Tag();
        var metadata = new Metadata();
        metadata.setName(name);
        tag.setMetadata(metadata);
        var spec = new Tag.TagSpec();
        spec.setDisplayName(displayName);
        spec.setSlug(name);
        tag.setSpec(spec);
        return client.create(tag).block();
    }

    Category createCategory(String name, String displayName) {
        var category = new Category();
        var metadata = new Metadata();
        metadata.setName(name);
        category.setMetadata(metadata);
        var spec = new Category.CategorySpec();
        spec.setDisplayName(displayName);
        spec.setSlug(name);
        spec.setPriority(0);
        category.setSpec(spec);
        return client.create(category).block();
    }

    User createUser(String name, String displayName) {
        var user = new User();
        var metadata = new Metadata();
        metadata.setName(name);
        user.setMetadata(metadata);
        user.getSpec().setDisplayName(displayName);
        user.getSpec().setAvatar("/avatars/" + name + ".png");
        return client.create(user).block();
    }

    void createPost(String name, String owner, List<Tag> tags, List<Category> categories) {
        var post = new Post();
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setLabels(Map.of(Post.PUBLISHED_LABEL, Boolean.TRUE.toString()));
        post.setMetadata(metadata);
        var spec = new Post.PostSpec();
        spec.setTitle("IT Post " + suffix);
        spec.setSlug(name);
        spec.setOwner(owner);
        spec.setTags(tags.stream().map(tag -> tag.getMetadata().getName()).toList());
        spec.setCategories(categories.stream()
                .map(category -> category.getMetadata().getName())
                .toList());
        spec.setDeleted(false);
        spec.setPublish(true);
        spec.setPinned(false);
        spec.setAllowComment(true);
        spec.setVisible(Post.VisibleEnum.PUBLIC);
        spec.setPriority(0);
        spec.setExcerpt(excerpt());
        post.setSpec(spec);
        post.getStatusOrDefault().setContributors(List.of(owner, "missing-contributor-" + suffix));
        client.create(post).block();
    }

    void createCounter(String name, int visit, int upvote, int totalComment, int approvedComment) {
        var counter = new Counter();
        var metadata = new Metadata();
        metadata.setName(name);
        counter.setMetadata(metadata);
        counter.setVisit(visit);
        counter.setUpvote(upvote);
        counter.setTotalComment(totalComment);
        counter.setApprovedComment(approvedComment);
        client.create(counter).block();
    }

    static Post.Excerpt excerpt() {
        var excerpt = new Post.Excerpt();
        excerpt.setAutoGenerate(true);
        excerpt.setRaw("");
        return excerpt;
    }
}
