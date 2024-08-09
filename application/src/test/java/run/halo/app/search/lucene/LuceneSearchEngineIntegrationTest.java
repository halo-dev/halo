package run.halo.app.search.lucene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static run.halo.app.core.extension.content.Post.VisibleEnum.PRIVATE;
import static run.halo.app.core.extension.content.Post.VisibleEnum.PUBLIC;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.support.RetryTemplateBuilder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;
import run.halo.app.content.Content;
import run.halo.app.content.ContentUpdateParam;
import run.halo.app.content.PostRequest;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.search.SearchEngine;
import run.halo.app.search.SearchOption;
import run.halo.app.search.SearchResult;

@DirtiesContext
@SpringBootTest(properties = {
    "halo.search-engine.lucene.enabled=true",
    "halo.extension.controller.disabled=false"})
@AutoConfigureWebTestClient
public class LuceneSearchEngineIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    PostService postService;

    @Autowired
    ReactiveExtensionClient client;

    @Autowired
    SearchEngine searchEngine;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        searchEngine.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = AnonymousUserConst.Role)
    void shouldSearchPostAfterPostPublished() {
        var postName = "first-post";
        assertNoResult(1);
        createPost(postName);
        assertHasResult(5);
        unpublishPost(postName);
        assertNoResult(5);
        publishPost(postName);
        assertHasResult(5);
        privatePost(postName);
        assertNoResult(5);
        publicPost(postName);
        assertHasResult(5);
        recyclePost(postName);
        assertNoResult(5);
        recoverPost(postName);
        assertHasResult(5);
        deletePostPermanently(postName);
        assertNoResult(5);
    }

    void assertHasResult(int maxAttempts) {
        var retryTemplate = new RetryTemplateBuilder()
            .exponentialBackoff(Duration.ofMillis(200), 2.0, Duration.ofSeconds(10))
            .maxAttempts(maxAttempts)
            .retryOn(AssertionFailedError.class)
            .build();
        var option = new SearchOption();
        option.setKeyword("halo");
        option.setHighlightPreTag("<my-tag>");
        option.setHighlightPostTag("</my-tag>");
        retryTemplate.execute(context -> {
            webClient.post().uri("/apis/api.halo.run/v1alpha1/indices/-/search")
                .bodyValue(option)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SearchResult.class).value(result -> {
                    assertEquals(1, result.getTotal());
                    assertEquals("halo", result.getKeyword());
                    var hits = result.getHits();
                    assertEquals(1, hits.size());
                    var doc = hits.get(0);
                    assertEquals("post.content.halo.run-first-post", doc.getId());
                    assertEquals("post.content.halo.run", doc.getType());
                    assertEquals("first <my-tag>halo</my-tag> post", doc.getTitle());
                    assertNull(doc.getDescription());
                    assertEquals("<my-tag>halo</my-tag>", doc.getContent());
                });
            return null;
        });
    }

    void assertNoResult(int maxAttempts) {
        var retryTemplate = new RetryTemplateBuilder()
            .exponentialBackoff(Duration.ofMillis(200), 2.0, Duration.ofSeconds(10))
            .maxAttempts(maxAttempts)
            .retryOn(AssertionFailedError.class)
            .build();

        var option = new SearchOption();
        option.setKeyword("halo");
        option.setHighlightPreTag("<my-tag>");
        option.setHighlightPostTag("</my-tag>");
        option.setIncludeTagNames(List.of("search"));
        option.setIncludeCategoryNames(List.of("halo"));
        option.setIncludeOwnerNames(List.of("admin"));
        retryTemplate.execute(context -> {
            webClient.post().uri("/apis/api.halo.run/v1alpha1/indices/-/search")
                .bodyValue(option)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SearchResult.class).value(result -> {
                    assertEquals(0, result.getTotal());
                    assertEquals("halo", result.getKeyword());
                });
            return null;
        });
    }

    void deletePostPermanently(String postName) {
        client.get(Post.class, postName)
            .flatMap(client::delete)
            .retryWhen(optimisticLockRetry())
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    void recoverPost(String postName) {
        client.get(Post.class, postName)
            .doOnNext(post -> post.getSpec().setDeleted(false))
            .flatMap(client::update)
            .retryWhen(optimisticLockRetry())
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    void recyclePost(String postName) {
        client.get(Post.class, postName)
            .doOnNext(post -> post.getSpec().setDeleted(true))
            .flatMap(client::update)
            .retryWhen(optimisticLockRetry())
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    void publicPost(String postName) {
        client.get(Post.class, postName)
            .doOnNext(post -> post.getSpec().setVisible(PUBLIC))
            .flatMap(client::update)
            .retryWhen(optimisticLockRetry())
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    void privatePost(String postName) {
        client.get(Post.class, postName)
            .doOnNext(post -> post.getSpec().setVisible(PRIVATE))
            .flatMap(client::update)
            .retryWhen(optimisticLockRetry())
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    void publishPost(String postName) {
        client.get(Post.class, postName)
            .flatMap(postService::publish)
            .retryWhen(optimisticLockRetry())
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    void unpublishPost(String postName) {
        client.get(Post.class, postName)
            .flatMap(postService::unpublish)
            .retryWhen(optimisticLockRetry())
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    void createPost(String postName) {
        var post = new Post();
        var metadata = new Metadata();
        post.setMetadata(metadata);
        metadata.setName(postName);
        var spec = new Post.PostSpec();
        post.setSpec(spec);
        spec.setPublish(true);
        spec.setOwner("admin");
        spec.setTitle("first halo post");
        spec.setVisible(PUBLIC);
        spec.setAllowComment(true);
        spec.setPinned(false);
        spec.setPriority(0);
        spec.setSlug("/first-post");
        spec.setDeleted(false);
        spec.setTags(List.of("search"));
        spec.setCategories(List.of("halo"));
        var excerpt = new Post.Excerpt();
        excerpt.setRaw("first post description");
        excerpt.setAutoGenerate(false);
        spec.setExcerpt(excerpt);
        var content = new Content("halo", "halo", "Markdown");
        var contentParam = ContentUpdateParam.from(content);
        var postRequest = new PostRequest(post, contentParam);
        postService.draftPost(postRequest)
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    Retry optimisticLockRetry() {
        return Retry.backoff(5, Duration.ofMillis(100))
            .filter(OptimisticLockingFailureException.class::isInstance);
    }

}
