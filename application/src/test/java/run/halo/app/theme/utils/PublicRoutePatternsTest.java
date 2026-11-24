package run.halo.app.theme.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriUtils;

class PublicRoutePatternsTest {

    @Test
    void extractsStaticPrefixesWithoutTreatingVariablesAsSegments() {
        assertThat(PublicRoutePatterns.firstSegment("/authors/{name}/page/{page:\\d+}"))
                .isEqualTo("authors");
        assertThat(PublicRoutePatterns.staticPrefix("/archives/{year:\\d{4}}")).isEqualTo("archives");
        assertThat(PublicRoutePatterns.staticPrefix("/{slug}")).isEmpty();
    }

    @Test
    void claimsOnlyStaticLocalizablePrefixes() {
        assertThat(PublicRoutePatterns.claimedPrefixes(
                        List.of("/{slug}", "/page/{page:\\d+}", "/journal/{year:\\d{4}}")))
                .isEqualTo(Set.of("page", "journal"));
    }

    @Test
    void acceptsHyphenatedLanguagePrefixes() {
        assertThat(PublicRoutePatterns.staticPrefix("/zh-hant/posts/hello")).isEqualTo("zh-hant");
        assertThat(PublicRoutePatterns.staticPrefix("/pt-br")).isEqualTo("pt-br");
        assertThat(PublicRoutePatterns.staticPrefix("/zh--hant")).isEmpty();
        assertThat(PublicRoutePatterns.staticPrefix("/-zh")).isEmpty();
    }

    @Test
    void matchesReadRequestsAgainstPublicPatterns() {
        var patterns = List.of("/posts/{slug}", "/page/{page:\\d+}");

        assertThat(PublicRoutePatterns.matchesAny("/posts/hello", HttpMethod.GET, patterns))
                .isTrue();
        assertThat(PublicRoutePatterns.matchesAny("/", HttpMethod.GET, List.of("/")))
                .isTrue();
        assertThat(PublicRoutePatterns.matchesAny("/page/2", HttpMethod.HEAD, patterns))
                .isTrue();
        assertThat(PublicRoutePatterns.matchesAny("/posts/hello", HttpMethod.POST, patterns))
                .isFalse();
        assertThat(PublicRoutePatterns.matchesAny("/other", HttpMethod.GET, patterns))
                .isFalse();
    }

    @Test
    void extractsVariablesFromMatchingRoutePatterns() {
        var patterns = List.of("/labels/{slug}", "/labels/{slug}/page/{page:\\d+}");

        assertThat(PublicRoutePatterns.matchAndExtract("/labels/news/page/2", patterns))
                .hasValueSatisfying(variables ->
                        assertThat(variables).containsEntry("slug", "news").containsEntry("page", "2"));
        assertThat(PublicRoutePatterns.matchAndExtract("/labels/%E6%A0%87%E7%AD%BE", List.of("/labels/{slug}")))
                .hasValueSatisfying(variables -> assertThat(variables).containsEntry("slug", "标签"));
        assertThat(PublicRoutePatterns.matchAndExtract("/labels/news/other", patterns))
                .isEmpty();
    }

    @Test
    void comparesDecodedAndEncodedRoutePaths() {
        var routePath = "/关于/团队";
        var encodedPath = UriUtils.encodePath(routePath, UTF_8);

        assertThat(PublicRoutePatterns.samePath(encodedPath, routePath)).isTrue();
        assertThat(PublicRoutePatterns.samePath(routePath, encodedPath)).isTrue();
        assertThat(PublicRoutePatterns.samePath("/关于/其他", routePath)).isFalse();
        assertThat(PublicRoutePatterns.samePath("/broken/%", routePath)).isFalse();
    }
}
