package run.halo.app.search;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.Validator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.localization.DefaultPublicApiLocalizationFilter;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.LocalizationScope;
import run.halo.app.localization.PublicApiLocalizationFilter;
import run.halo.app.localization.PublicApiLocalizationResolver;
import run.halo.app.localization.ResourceLocalizationProvider;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.lucene.LuceneSearchEngine;

class IndexEndpointLocalizationTest {

    @TempDir
    Path directory;

    @Test
    void queryLocaleEntersLocalizationContext() {
        var searchService = mock(SearchService.class);
        when(searchService.search(any(SearchOption.class)))
                .thenReturn(Mono.defer(() -> LocalizationContext.current()
                        .filter(context -> context.targetLocale().equals(Locale.ENGLISH))
                        .map(context -> new SearchResult())
                        .switchIfEmpty(Mono.error(new AssertionError("Missing localization context")))));
        var extensions = mock(ExtensionGetter.class);
        var provider = mock(ResourceLocalizationProvider.class);
        when(extensions.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
        when(provider.resolveContext(Locale.ENGLISH, LocalizationScope.PUBLIC_API))
                .thenReturn(Mono.just(context("en")));
        var filter = new DefaultPublicApiLocalizationFilter(new PublicApiLocalizationResolver(extensions));
        var endpoint = new IndexEndpoint(searchService, filter);

        WebTestClient.bindToRouterFunction(endpoint.endpoint())
                .build()
                .post()
                .uri("/indices/-/search?locale=en")
                .bodyValue(Map.of("keyword", "translationkeyword"))
                .exchange()
                .expectStatus()
                .isOk();

        verify(provider).resolveContext(Locale.ENGLISH, LocalizationScope.PUBLIC_API);
    }

    @Test
    void bodyLocaleCannotOverrideTrustedOrInactiveLanguage() throws Exception {
        var engine = new LuceneSearchEngine(directory);
        engine.afterPropertiesSet();
        try {
            engine.addOrUpdate(List.of(translatedDocument()));
            var extensions = mock(ExtensionGetter.class);
            when(extensions.getEnabledExtension(SearchEngine.class)).thenReturn(Mono.just(engine));
            var enabled = new AtomicBoolean(true);
            HaloDocumentVariantProvider provider = source -> Flux.empty();
            when(extensions.getEnabledExtension(HaloDocumentVariantProvider.class))
                    .thenReturn(Mono.defer(() -> enabled.get() ? Mono.just(provider) : Mono.empty()));
            var validator = Validator.forInstanceOf(SearchOption.class, (option, errors) -> {});
            PublicApiLocalizationFilter passthrough = (request, next) -> next.handle(request);
            var endpoint = new IndexEndpoint(new SearchServiceImpl(validator, extensions), passthrough);

            search(client(endpoint, null), "en", 0);
            search(client(endpoint, context("zh-CN")), "en", 0);
            search(client(endpoint, context("en")), "de", 1);

            enabled.set(false);
            search(client(endpoint, context("en")), "en", 0);
        } finally {
            engine.destroy();
        }
    }

    private static void search(WebTestClient client, String submittedLocale, int expectedResults) {
        client.post()
                .uri("/indices/-/search")
                .bodyValue(Map.of("keyword", "translationkeyword", "locale", submittedLocale))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.total")
                .isEqualTo(expectedResults);
    }

    private static WebTestClient client(IndexEndpoint endpoint, LocalizationContext context) {
        var client = WebTestClient.bindToRouterFunction(endpoint.endpoint());
        if (context != null) {
            client.webFilter((exchange, chain) ->
                    chain.filter(exchange).contextWrite(c -> c.put(LocalizationContext.class, context)));
        }
        return client.build();
    }

    private static LocalizationContext context(String language) {
        return new LocalizationContext(
                Locale.SIMPLIFIED_CHINESE, Locale.forLanguageTag(language), List.of(), LocalizationScope.PUBLIC_API);
    }

    private static HaloDocument translatedDocument() {
        var doc = new HaloDocument();
        doc.setId("post:locale:en");
        doc.setSourceId("post");
        doc.setLocale("en");
        doc.setMetadataName("post");
        doc.setType("post.content.halo.run");
        doc.setTitle("translationkeyword");
        doc.setContent("translationkeyword");
        doc.setOwnerName("admin");
        doc.setCreationTimestamp(Instant.EPOCH);
        doc.setPermalink("/en/archives/post");
        doc.setPublished(true);
        doc.setExposed(true);
        return doc;
    }
}
