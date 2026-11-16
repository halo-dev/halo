package run.halo.app.search;

import static org.mockito.Mockito.mock;
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
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.LocalizationScope;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.lucene.LuceneSearchEngine;

class IndexEndpointLocalizationTest {

    @TempDir
    Path directory;

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
            var endpoint = new IndexEndpoint(new SearchServiceImpl(validator, extensions));

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
