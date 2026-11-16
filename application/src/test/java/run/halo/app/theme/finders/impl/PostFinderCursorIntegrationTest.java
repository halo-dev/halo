package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.dialect.SpringStandardDialect;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionStoreUtil;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexEngine;
import run.halo.app.extension.store.ReactiveExtensionStoreClient;
import run.halo.app.theme.ReactiveSpelVariableExpressionEvaluator;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.ListedPostVo;

@DirtiesContext
@SpringBootTest(properties = "halo.work-dir=${java.io.tmpdir}/halo-post-cursor-test")
class PostFinderCursorIntegrationTest {
    private static final Instant TIME = Instant.parse("2026-01-01T00:00:00Z");
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    @Autowired
    PostFinder finder;

    @Autowired
    ReactiveExtensionClient client;

    @Autowired
    ReactiveExtensionStoreClient storeClient;

    @Autowired
    SchemeManager schemeManager;

    @Autowired
    IndexEngine indexEngine;

    private final List<Extension> created = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (var extension : created.reversed()) {
            var name = ExtensionStoreUtil.buildStoreName(
                    schemeManager.get(extension.getClass()),
                    extension.getMetadata().getName());
            var stored = storeClient.fetchByName(name).block(TIMEOUT);
            if (stored != null) {
                storeClient.delete(name, stored.getVersion()).block(TIMEOUT);
            }
            indexEngine.delete(List.of(extension));
        }
    }

    @Test
    void customSortMatchesListIncludingTiesAndIgnoresPagination() {
        post("a", p -> p.getStatus().setLastModifyTime(TIME.minusSeconds(1)));
        post("b", p -> {});
        post("c", p -> {});
        var descending = Map.<String, Object>of("sort", List.of("status.lastModifyTime,desc"));
        assertList(descending, "b", "c", "a");
        assertNavigation("c", descending, "a", "b");
        assertNavigation("c", Map.of("sort", List.of("status.lastModifyTime,desc"), "page", 2, "size", 1), "a", "b");
        var ascending = Map.<String, Object>of("sort", List.of("status.lastModifyTime,asc"));
        assertList(ascending, "a", "b", "c");
        assertNavigation("b", ascending, "c", "a");
    }

    @Test
    void defaultSortIncludesPinnedPriorityAndName() {
        post("a", p -> {});
        post("b", p -> p.getSpec().setPriority(5));
        post("c", p -> p.getSpec().setPinned(true));
        assertList(Map.of(), "c", "b", "a");
        assertNavigation("b", Map.of(), "a", "c");
        assertNavigation("b", null, "a", "c");
    }

    @Test
    void handlesBoundariesMissingCurrentAndEmptyResults() {
        assertNavigation("missing", Map.of(), null, null);
        post("a", p -> {});
        assertNavigation("a", Map.of(), null, null);
        post("b", p -> {});
        post("c", p -> {});
        assertNavigation("a", Map.of(), "b", null);
        assertNavigation("c", Map.of(), null, "b");
        assertNavigation("missing", Map.of(), null, null);
        assertNavigation("b", Map.of("tagName", "missing"), null, null);
    }

    @Test
    void combinesFiltersAndIncludesEligibleChildCategories() {
        category("root", null, false);
        category("child", "root", false);
        category("independent", "root", true);
        post("a", p -> p.getSpec().setCategories(List.of("root")));
        post("b", p -> p.getSpec().setCategories(List.of("child")));
        post("c", p -> p.getSpec().setCategories(List.of("root")));
        post("d", p -> p.getSpec().setCategories(List.of("independent")));
        post("e", p -> p.getSpec().setCategories(List.of("unrelated")));
        post("f", p -> p.getSpec().setTags(List.of("other")));
        post("g", p -> p.getSpec().setOwner("other"));
        post("h", p -> p.getSpec().setPinned(true));
        var params =
                Map.<String, Object>of("categoryName", "root", "tagName", "tag", "owner", "author", "pinned", false);
        assertList(params, "a", "b", "c");
        assertNavigation("b", params, "c", "a");
        assertNavigation("d", params, null, null);
        assertNavigation("f", params, null, null);
        assertNavigation("g", params, null, null);
        assertNavigation("h", params, null, null);
    }

    @Test
    void visibilityAndHiddenRulesMatchList() {
        post("a", p -> {});
        post("b", p -> {});
        post("c", p -> {});
        post("d", p -> p.getMetadata().getLabels().clear());
        post("e", p -> p.getSpec().setVisible(Post.VisibleEnum.PRIVATE));
        post("f", p -> p.getSpec().setDeleted(true));
        post("g", p -> {});
        client.delete(client.get(Post.class, "g").block(TIMEOUT)).block(TIMEOUT);
        post("h", p -> p.getStatus().setHideFromList(true));
        assertList(Map.of(), "a", "b", "c");
        assertNavigation("b", Map.of(), "c", "a");
        for (var name : List.of("d", "e", "f", "g", "h")) {
            assertNavigation(name, Map.of(), null, null);
        }
        // Explicit tag queries include hidden posts, just like list(params).
        assertList(Map.of("tagName", "tag"), "a", "b", "c", "h");
        assertNavigation("c", Map.of("tagName", "tag"), "h", "b");
    }

    @Test
    void ownerVisibilityMatchesList() {
        post("a", p -> {});
        post("b", p -> p.getSpec().setVisible(Post.VisibleEnum.PRIVATE));
        post("c", p -> {});
        var authentication = UsernamePasswordAuthenticationToken.authenticated("author", "unused", List.of());
        var securityContext = ReactiveSecurityContextHolder.withAuthentication(authentication);
        var list = finder.list(Map.of()).contextWrite(securityContext).block(TIMEOUT);
        assertThat(list).isNotNull();
        assertThat(list.getItems())
                .extracting(PostFinderCursorIntegrationTest::name)
                .containsExactly("a", "b", "c");
        var navigation =
                finder.cursor("b", Map.of()).contextWrite(securityContext).block(TIMEOUT);
        assertThat(navigation).isNotNull();
        assertThat(name(navigation.previous())).isEqualTo("c");
        assertThat(name(navigation.next())).isEqualTo("a");
        assertNavigation("b", Map.of(), null, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"asc", "desc"})
    void nullSortValuesFollowListOrdering(String direction) {
        post("a", p -> {
            p.getSpec().setPublishTime(null);
            p.getStatus().setLastModifyTime(null);
        });
        post("b", p -> {});
        post("c", p -> {});
        var params = Map.<String, Object>of("sort", List.of("status.lastModifyTime," + direction));
        if ("asc".equals(direction)) {
            assertList(params, "a", "b", "c");
            assertNavigation("a", params, "b", null);
        } else {
            assertList(params, "b", "c", "a");
            assertNavigation("a", params, null, "c");
        }
    }

    @Test
    void existingCursorsKeepPublishTimeAndPrimaryCategoryBehavior() {
        category("root", null, false);
        post("a", p -> p.getSpec().setPublishTime(TIME.minusSeconds(1)));
        post("b", p -> {});
        post("c", p -> p.getSpec().setPublishTime(TIME.plusSeconds(1)));
        post("d", p -> p.getSpec().setCategories(List.of("other")));
        var global = finder.cursor("b").block(TIMEOUT);
        var category = finder.cursorByCategory("b").block(TIMEOUT);
        assertThat(global).isNotNull();
        assertThat(category).isNotNull();
        assertThat(name(global.previous())).isEqualTo("a");
        assertThat(name(global.next())).isEqualTo("c");
        assertThat(name(category.previous())).isEqualTo("a");
        assertThat(name(category.next())).isEqualTo("c");
    }

    @Test
    void templateCanInvokeMapOverloadAndRenderAdjacentLinks() {
        post("a", p -> {});
        post("b", p -> {});
        post("c", p -> {});
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(new StringTemplateResolver());
        engine.setDialect(new SpringStandardDialect() {
            @Override
            public IStandardVariableExpressionEvaluator getVariableExpressionEvaluator() {
                return ReactiveSpelVariableExpressionEvaluator.INSTANCE;
            }
        });
        var context = new Context();
        context.setVariable("postFinder", finder);
        var html = engine.process("""
                <nav th:with="cursor=${postFinder.cursor('b', {tagName: 'tag', sort: {'metadata.name,asc'}})}">
                  <a th:href="${cursor.previous.status.permalink}">Previous</a>
                  <a th:href="${cursor.next.status.permalink}">Next</a>
                </nav>
                """, context);
        assertThat(html).isEqualToIgnoringWhitespace("""
                <nav><a href="/archives/c">Previous</a><a href="/archives/a">Next</a></nav>
                """);
    }

    private void assertList(Map<String, Object> params, String... expected) {
        var result = finder.list(params).block(TIMEOUT);
        assertThat(result).isNotNull();
        assertThat(result.getItems())
                .extracting(PostFinderCursorIntegrationTest::name)
                .containsExactly(expected);
    }

    private void assertNavigation(String current, Map<String, Object> params, String previous, String next) {
        var result = finder.cursor(current, params).block(TIMEOUT);
        assertThat(result).isNotNull();
        assertThat(name(result.previous()))
                .as("previous for %s with %s; navigation=%s", current, params, result)
                .isEqualTo(previous);
        assertThat(name(result.next())).isEqualTo(next);
    }

    private static String name(ListedPostVo post) {
        return post == null ? null : post.getMetadata().getName();
    }

    private void post(String name, Consumer<Post> customize) {
        var post = new Post();
        post.setMetadata(metadata(name));
        post.getMetadata().setLabels(new HashMap<>(Map.of(Post.PUBLISHED_LABEL, "true")));
        var spec = new Post.PostSpec();
        spec.setTitle(name);
        spec.setSlug(name);
        spec.setOwner("author");
        spec.setCategories(List.of("root"));
        spec.setTags(List.of("tag"));
        spec.setVisible(Post.VisibleEnum.PUBLIC);
        spec.setDeleted(false);
        spec.setPublish(true);
        spec.setPinned(false);
        spec.setPriority(0);
        spec.setAllowComment(true);
        spec.setPublishTime(TIME);
        spec.setExcerpt(new Post.Excerpt());
        spec.getExcerpt().setAutoGenerate(true);
        post.setSpec(spec);
        post.getStatusOrDefault().setLastModifyTime(TIME);
        post.getStatus().setPermalink("/archives/" + name);
        customize.accept(post);
        save(post);
    }

    private void category(String name, String parent, boolean independent) {
        var category = new Category();
        category.setMetadata(metadata(name));
        var spec = new Category.CategorySpec();
        spec.setDisplayName(name);
        spec.setSlug(name);
        spec.setPriority(0);
        spec.setParent(parent);
        spec.setPreventParentPostCascadeQuery(independent);
        category.setSpec(spec);
        save(category);
    }

    private Metadata metadata(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        return metadata;
    }

    private <T extends Extension> void save(T resource) {
        created.add(client.create(resource).block(TIMEOUT));
    }
}
