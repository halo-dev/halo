package run.halo.app.theme.router;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting.ThemeRouteRules;
import run.halo.app.theme.router.factories.ArchiveRouteFactory;
import run.halo.app.theme.router.factories.AuthorPostsRouteFactory;
import run.halo.app.theme.router.factories.CategoriesRouteFactory;
import run.halo.app.theme.router.factories.CategoryPostRouteFactory;
import run.halo.app.theme.router.factories.IndexRouteFactory;
import run.halo.app.theme.router.factories.PostRouteFactory;
import run.halo.app.theme.router.factories.TagPostRouteFactory;
import run.halo.app.theme.router.factories.TagsRouteFactory;

@ExtendWith(MockitoExtension.class)
class ThemeCompositeRouterFunctionTest {

    @Mock
    private SystemConfigFetcher environmentFetcher;

    @Mock
    private ArchiveRouteFactory archiveRouteFactory;

    @Mock
    private PostRouteFactory postRouteFactory;

    @Mock
    private CategoriesRouteFactory categoriesRouteFactory;

    @Mock
    private CategoryPostRouteFactory categoryPostRouteFactory;

    @Mock
    private TagPostRouteFactory tagPostRouteFactory;

    @Mock
    private TagsRouteFactory tagsRouteFactory;

    @Mock
    private AuthorPostsRouteFactory authorPostsRouteFactory;

    @Mock
    private IndexRouteFactory indexRouteFactory;

    @InjectMocks
    private ThemeCompositeRouterFunction routerFunction;

    @BeforeEach
    void setUp() {
        var rules = new ThemeRouteRules();
        rules.setArchives("/journal");
        rules.setCategories("/topics");
        rules.setTags("/labels");
        rules.setPost("/{slug}");
        when(environmentFetcher.fetchRouteRules()).thenReturn(Mono.just(rules));

        lenient().when(archiveRouteFactory.claimedPrefixes(anyString())).thenReturn(Mono.just(Set.of("journal")));
        lenient().when(postRouteFactory.claimedPrefixes(anyString())).thenReturn(Mono.just(Set.of("news")));
        lenient().when(categoriesRouteFactory.claimedPrefixes(anyString())).thenReturn(Mono.just(Set.of("topics")));
        lenient().when(categoryPostRouteFactory.claimedPrefixes(anyString())).thenReturn(Mono.just(Set.of()));
        lenient().when(tagPostRouteFactory.claimedPrefixes(anyString())).thenReturn(Mono.just(Set.of("labels")));
        lenient().when(tagsRouteFactory.claimedPrefixes(anyString())).thenReturn(Mono.just(Set.of()));
        lenient().when(authorPostsRouteFactory.claimedPrefixes(anyString())).thenReturn(Mono.just(Set.of("authors")));
        lenient().when(indexRouteFactory.claimedPrefixes(anyString())).thenReturn(Mono.just(Set.of("page")));

        lenient()
                .when(archiveRouteFactory.matches(anyString(), anyString(), any(HttpMethod.class)))
                .thenReturn(Mono.just(false));
        lenient()
                .when(postRouteFactory.matches(anyString(), anyString(), any(HttpMethod.class)))
                .thenReturn(Mono.just(false));
        lenient()
                .when(categoriesRouteFactory.matches(anyString(), anyString(), any(HttpMethod.class)))
                .thenReturn(Mono.just(false));
        lenient()
                .when(categoryPostRouteFactory.matches(anyString(), anyString(), any(HttpMethod.class)))
                .thenReturn(Mono.just(false));
        lenient()
                .when(tagPostRouteFactory.matches(anyString(), anyString(), any(HttpMethod.class)))
                .thenReturn(Mono.just(false));
        lenient()
                .when(tagsRouteFactory.matches(anyString(), anyString(), any(HttpMethod.class)))
                .thenReturn(Mono.just(false));
        lenient()
                .when(authorPostsRouteFactory.matches(anyString(), anyString(), any(HttpMethod.class)))
                .thenReturn(Mono.just(false));
        lenient()
                .when(indexRouteFactory.matches(anyString(), anyString(), any(HttpMethod.class)))
                .thenReturn(Mono.just(false));
    }

    @Test
    void aggregatesClaimedPrefixesFromRegisteredRoutes() {
        StepVerifier.create(routerFunction.claimedPrefixes())
                .assertNext(prefixes -> assertThat(prefixes)
                        .containsExactlyInAnyOrder("journal", "news", "topics", "labels", "authors", "page"))
                .verifyComplete();
    }

    @Test
    void delegatesMatchingToTheOwningRouteFactory() {
        when(tagPostRouteFactory.matches(anyString(), eq("/labels/one"), any(HttpMethod.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(routerFunction.matches("/labels/one", HttpMethod.GET))
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(routerFunction.matches("/labels/one", HttpMethod.POST))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void fallsBackAnInvalidRouteFieldWithoutDiscardingOtherDeclarations() {
        var rules = new ThemeRouteRules();
        rules.setArchives("/journal");
        rules.setCategories("/topics");
        rules.setTags("/");
        rules.setPost("/{slug}");
        when(environmentFetcher.fetchRouteRules()).thenReturn(Mono.just(rules));
        when(tagsRouteFactory.claimedPrefixes("/tags")).thenReturn(Mono.just(Set.of("tags")));
        when(tagPostRouteFactory.claimedPrefixes("/tags")).thenReturn(Mono.just(Set.of()));
        when(tagPostRouteFactory.matches(eq("/tags"), eq("/tags/one"), eq(HttpMethod.GET)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(routerFunction.claimedPrefixes())
                .assertNext(prefixes -> assertThat(prefixes).contains("journal", "topics", "tags", "authors", "page"))
                .verifyComplete();
        StepVerifier.create(routerFunction.matches("/tags/one", HttpMethod.GET))
                .expectNext(true)
                .verifyComplete();
        verify(archiveRouteFactory).claimedPrefixes("/journal");
        verify(categoriesRouteFactory).claimedPrefixes("/topics");
        verify(tagsRouteFactory).claimedPrefixes("/tags");
    }

    @Test
    void keepsDefaultRouteOwnershipWhenRulesCannotBeRead() {
        when(environmentFetcher.fetchRouteRules())
                .thenReturn(Mono.error(new IllegalStateException("route rules unavailable")));
        when(archiveRouteFactory.claimedPrefixes("/archives")).thenReturn(Mono.just(Set.of("archives")));
        when(categoriesRouteFactory.claimedPrefixes("/categories")).thenReturn(Mono.just(Set.of("categories")));
        when(tagsRouteFactory.claimedPrefixes("/tags")).thenReturn(Mono.just(Set.of("tags")));

        StepVerifier.create(routerFunction.claimedPrefixes())
                .assertNext(
                        prefixes -> assertThat(prefixes).contains("archives", "categories", "tags", "authors", "page"))
                .verifyComplete();
        verify(postRouteFactory).claimedPrefixes("/archives/{slug}");
    }
}
