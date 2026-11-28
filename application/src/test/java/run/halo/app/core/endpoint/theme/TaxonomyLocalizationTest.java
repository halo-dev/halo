package run.halo.app.core.endpoint.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.*;
import run.halo.app.localization.*;
import run.halo.app.localization.PresentationTestSupport;
import run.halo.app.theme.finders.*;
import run.halo.app.theme.finders.impl.TagFinderImpl;

class TaxonomyLocalizationTest {
    private final ReactiveExtensionClient client = mock(ReactiveExtensionClient.class);
    private final ResourceLocalizationService service = mock(ResourceLocalizationService.class);
    private final LocalizationContext context =
            new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_API);

    @Test
    void categoryApiProjectsDetailAndPageWithoutChangingQueryParameters() {
        var source = category("分类");
        var translated = category("Category");
        when(client.get(Category.class, "category")).thenReturn(Mono.just(source));
        when(client.listBy(eq(Category.class), any(), any()))
                .thenReturn(Mono.just(new ListResult<>(2, 3, 10, List.of(source))));
        when(service.localizeAll(List.of(source), context)).thenReturn(Mono.just(List.of(translated)));
        var endpoint = new CategoryQueryEndpoint(
                PresentationTestSupport.client(client, service), mock(PostPublicQueryService.class));
        var web = WebTestClient.bindToRouterFunction(endpoint.endpoint())
                .webFilter((exchange, chain) ->
                        chain.filter(exchange).contextWrite(ctx -> ctx.put(LocalizationContext.class, context)))
                .build();
        web.get()
                .uri("/categories/category")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.spec.displayName")
                .isEqualTo("Category")
                .jsonPath("$.metadata.name")
                .isEqualTo("category");
        web.get()
                .uri("/categories?page=2&size=3&sort=spec.priority,desc&labelSelector=public=true")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.items[0].spec.displayName")
                .isEqualTo("Category")
                .jsonPath("$.total")
                .isEqualTo(10)
                .jsonPath("$.page")
                .isEqualTo(2);
        verifyQuery(Category.class);
        assertThat(source.getSpec().getDisplayName()).isEqualTo("分类");
    }

    @Test
    void tagApiProjectsPageBeforeSynchronousVoConversion() {
        var source = tag("标签");
        var translated = tag("Tag");
        when(client.listBy(eq(Tag.class), any(), any()))
                .thenReturn(Mono.just(new ListResult<>(2, 3, 10, List.of(source))));
        when(service.localizeAll(List.of(source), context)).thenReturn(Mono.just(List.of(translated)));
        var endpoint = new TagQueryEndpoint(
                PresentationTestSupport.client(client, service),
                new TagFinderImpl(PresentationTestSupport.client(client, service)),
                mock(PostPublicQueryService.class));
        var web = WebTestClient.bindToRouterFunction(endpoint.endpoint())
                .webFilter((exchange, chain) ->
                        chain.filter(exchange).contextWrite(ctx -> ctx.put(LocalizationContext.class, context)))
                .build();
        web.get()
                .uri("/tags?page=2&size=3&sort=spec.priority,desc&labelSelector=public=true")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.items[0].spec.displayName")
                .isEqualTo("Tag")
                .jsonPath("$.items[0].metadata.name")
                .isEqualTo("tag")
                .jsonPath("$.total")
                .isEqualTo(10)
                .jsonPath("$.page")
                .isEqualTo(2);
        verifyQuery(Tag.class);
        assertThat(source.getSpec().getDisplayName()).isEqualTo("标签");
    }

    private <T extends Extension> void verifyQuery(Class<T> type) {
        var options = ArgumentCaptor.forClass(ListOptions.class);
        var page = ArgumentCaptor.forClass(PageRequest.class);
        verify(client).listBy(eq(type), options.capture(), page.capture());
        assertThat(page.getValue().getPageNumber()).isEqualTo(2);
        assertThat(page.getValue().getPageSize()).isEqualTo(3);
        assertThat(page.getValue().getSort())
                .isEqualTo(Sort.by(Sort.Order.desc("spec.priority")).and(ExtensionUtil.defaultSort()));
        assertThat(options.getValue().getLabelSelector().toString()).contains("public");
    }

    private Category category(String title) {
        var value = new Category();
        var metadata = new Metadata();
        metadata.setName("category");
        value.setMetadata(metadata);
        value.setSpec(new Category.CategorySpec());
        value.getSpec().setDisplayName(title);
        return value;
    }

    private Tag tag(String title) {
        var value = new Tag();
        var metadata = new Metadata();
        metadata.setName("tag");
        value.setMetadata(metadata);
        value.setSpec(new Tag.TagSpec());
        value.getSpec().setDisplayName(title);
        return value;
    }
}
