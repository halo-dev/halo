package run.halo.app.theme.router.factories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.*;
import run.halo.app.localization.*;
import run.halo.app.localization.DefaultReactivePresentationExtensionClient;
import run.halo.app.localization.ResourcePresentationService;

@ExtendWith(MockitoExtension.class)
class CategoryPostRouteFactoryTest {
    @Mock
    ReactiveExtensionClient client;

    @Mock
    ResourcePresentationService presentation;

    @InjectMocks
    CategoryPostRouteFactory factory;

    @BeforeEach
    void wirePresentationClient() {
        ReflectionTestUtils.setField(
                factory, "client", new DefaultReactivePresentationExtensionClient(client, presentation));
    }

    @Test
    void projectsCategoryTemplateVariableWithoutChangingSlugLookup() {
        var source = category("原分类");
        var translated = category("Category");
        var context = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
        when(client.listBy(eq(Category.class), any(), any()))
                .thenReturn(Mono.just(new ListResult<>(1, 1, 1, List.of(source))));
        when(presentation.presentAll(List.of(source))).thenReturn(Mono.just(List.of(translated)));
        factory.fetchBySlug("source-slug")
                .contextWrite(ctx -> ctx.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(vo -> {
                    assertThat(vo.getSpec().getDisplayName()).isEqualTo("Category");
                    assertThat(vo.getSpec().getSlug()).isEqualTo("source-slug");
                    assertThat(vo.getMetadata().getName()).isEqualTo("category-name");
                })
                .verifyComplete();
        var options = ArgumentCaptor.forClass(ListOptions.class);
        verify(client).listBy(eq(Category.class), options.capture(), any());
        assertThat(options.getValue().toCondition().toString()).contains("source-slug", "deletionTimestamp");
        assertThat(source.getSpec().getDisplayName()).isEqualTo("原分类");
    }

    private Category category(String displayName) {
        var result = new Category();
        var metadata = new Metadata();
        metadata.setName("category-name");
        result.setMetadata(metadata);
        result.setSpec(new Category.CategorySpec());
        result.getSpec().setDisplayName(displayName);
        result.getSpec().setSlug("source-slug");
        return result;
    }
}
