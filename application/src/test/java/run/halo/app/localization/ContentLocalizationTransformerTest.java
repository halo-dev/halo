package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;
import run.halo.app.content.ContentWrapper;
import run.halo.app.extension.GroupVersionKind;

class ContentLocalizationTransformerTest {
    private final ResourceLocalizationService localization = mock(ResourceLocalizationService.class);
    private final ContentLocalizationTransformer transformer = new ContentLocalizationTransformer(localization);
    private final ResourceReference owner =
            new ResourceReference(new GroupVersionKind("test.local", "v1", "Note"), "one");

    @ParameterizedTest
    @ValueSource(strings = {"PUBLIC_PAGE", "PUBLIC_API"})
    void transformsCompleteContentInPublicScopes(String scopeName) {
        var context =
                new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.valueOf(scopeName));
        var source = ContentWrapper.builder()
                .snapshotName("release")
                .rawType("markdown")
                .raw("原文")
                .content("<p>原文</p>")
                .build();
        var fields = new LinkedHashMap<String, String>();
        fields.put("content.html", "<p>原文</p>");
        fields.put("content.raw", "原文");
        var translated = Map.of("content.html", "<p>Body</p>", "content.raw", "Body");
        when(localization.localizeFields(
                        owner,
                        fields,
                        List.of(new LocalizableDocument("content.html", "content.raw", "markdown", null)),
                        context))
                .thenReturn(Mono.just(translated));

        transformer
                .transform(owner, source)
                .contextWrite(Context.of(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertThat(result.getSnapshotName()).isEqualTo("release");
                    assertThat(result.getRawType()).isEqualTo("markdown");
                    assertThat(result.getRaw()).isEqualTo("Body");
                    assertThat(result.getContent()).isEqualTo("<p>Body</p>");
                })
                .verifyComplete();

        assertThat(source.getRaw()).isEqualTo("原文");
        assertThat(source.getContent()).isEqualTo("<p>原文</p>");
    }

    @Test
    void retainsSourceOutsidePublicScopes() {
        var source = ContentWrapper.builder()
                .rawType("markdown")
                .raw("原文")
                .content("<p>原文</p>")
                .build();
        var context = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.CONSOLE);

        transformer
                .transform(owner, source)
                .contextWrite(Context.of(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .expectNext(source)
                .verifyComplete();

        verifyNoInteractions(localization);
    }

    @Test
    void retainsSourceWhenDocumentFormatIsUnavailable() {
        var source = ContentWrapper.builder()
                .rawType(" ")
                .raw("Source")
                .content("<p>Source</p>")
                .build();
        var context = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);

        transformer
                .transform(owner, source)
                .contextWrite(Context.of(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .expectNext(source)
                .verifyComplete();

        verify(localization, org.mockito.Mockito.never())
                .localizeFields(
                        any(ResourceReference.class),
                        org.mockito.ArgumentMatchers.<Map<String, String>>any(),
                        org.mockito.ArgumentMatchers.<List<LocalizableDocument>>any(),
                        any(LocalizationContext.class));
    }
}
