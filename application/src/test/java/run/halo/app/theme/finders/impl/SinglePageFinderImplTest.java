package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.LocalizationScope;
import run.halo.app.localization.PresentationTestSupport;
import run.halo.app.localization.ResourceLocalizationService;
import run.halo.app.theme.finders.SinglePageConversionService;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.SinglePageVo;

/**
 * Tests for {@link SinglePageFinderImpl}.
 *
 * @author guqing
 * @since 2.0.1
 */
@ExtendWith(MockitoExtension.class)
class SinglePageFinderImplTest {

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private SinglePageConversionService singlePageConversionService;

    @Mock
    private ResourceLocalizationService localizationService;

    private SinglePageFinderImpl singlePageFinder;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        singlePageFinder = new SinglePageFinderImpl(
                PresentationTestSupport.client(client, localizationService), singlePageConversionService);
        lenient()
                .when(localizationService.localizeAll(anyList(), any()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    void getByName() {
        // fix gh-2992
        String fakePageName = "fake-page";
        SinglePage singlePage = new SinglePage();
        singlePage.setMetadata(new Metadata());
        singlePage.getMetadata().setName(fakePageName);
        singlePage.getMetadata().setLabels(Map.of(SinglePage.PUBLISHED_LABEL, "true"));
        singlePage.setSpec(new SinglePage.SinglePageSpec());
        singlePage.getSpec().setOwner("fake-owner");
        singlePage.getSpec().setReleaseSnapshot("fake-release");
        singlePage.getSpec().setPublish(true);
        singlePage.getSpec().setDeleted(false);
        singlePage.getSpec().setVisible(Post.VisibleEnum.PUBLIC);
        singlePage.setStatus(new SinglePage.SinglePageStatus());
        when(client.get(eq(SinglePage.class), eq(fakePageName))).thenReturn(Mono.just(singlePage));

        when(singlePageConversionService.convertToVo(eq(singlePage))).thenReturn(Mono.just(mock(SinglePageVo.class)));

        singlePageFinder
                .getByName(fakePageName)
                .as(StepVerifier::create)
                .consumeNextWith(page -> assertThat(page).isNotNull())
                .verifyComplete();

        verify(client).get(SinglePage.class, fakePageName);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void contentPreservesConversionServiceLookup(boolean localized) {
        var content = ContentVo.builder().content("released body").build();
        when(singlePageConversionService.getContent("private")).thenReturn(Mono.just(content));
        var request = singlePageFinder.content("private");
        if (localized) {
            request = request.contextWrite(context -> context.put(LocalizationContext.class, localizationContext()));
        }

        request.as(StepVerifier::create).expectNext(content).verifyComplete();

        verify(singlePageConversionService).getContent("private");
        verifyNoInteractions(client, localizationService);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void getByNameAppliesSourceVisibilityToPresentedPage(boolean localized) {
        var page = privatePage();
        when(client.get(SinglePage.class, "private")).thenReturn(Mono.just(page));
        var request = singlePageFinder.getByName("private");
        if (localized) {
            request = request.contextWrite(context -> context.put(LocalizationContext.class, localizationContext()));
        }

        request.as(StepVerifier::create).verifyComplete();
        var visitor = new TestingAuthenticationToken("visitor", "");
        request.contextWrite(ReactiveSecurityContextHolder.withAuthentication(visitor))
                .as(StepVerifier::create)
                .verifyComplete();
        verifyNoInteractions(singlePageConversionService);

        if (localized) {
            when(localizationService.localizeAll(List.of(page), localizationContext()))
                    .thenReturn(Mono.just(List.of(page)));
        }
        var pageVo = mock(SinglePageVo.class);
        when(singlePageConversionService.convertToVo(page)).thenReturn(Mono.just(pageVo));
        var owner = new TestingAuthenticationToken("owner", "");
        request.contextWrite(ReactiveSecurityContextHolder.withAuthentication(owner))
                .as(StepVerifier::create)
                .expectNext(pageVo)
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void getByNameRetainsExistingMetadataDeletionBehavior(boolean localized) {
        var page = privatePage();
        page.getSpec().setVisible(Post.VisibleEnum.PUBLIC);
        page.getMetadata().setDeletionTimestamp(Instant.parse("2026-11-16T00:00:00Z"));
        when(client.get(SinglePage.class, "private")).thenReturn(Mono.just(page));
        var pageVo = mock(SinglePageVo.class);
        when(singlePageConversionService.convertToVo(page)).thenReturn(Mono.just(pageVo));
        var request = singlePageFinder.getByName("private");
        if (localized) {
            when(localizationService.localizeAll(List.of(page), localizationContext()))
                    .thenReturn(Mono.just(List.of(page)));
            request = request.contextWrite(context -> context.put(LocalizationContext.class, localizationContext()));
        }

        request.as(StepVerifier::create).expectNext(pageVo).verifyComplete();
    }

    private static LocalizationContext localizationContext() {
        return new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
    }

    private static SinglePage privatePage() {
        var page = new SinglePage();
        page.setMetadata(new Metadata());
        page.getMetadata().setName("private");
        page.getMetadata().setLabels(Map.of(SinglePage.PUBLISHED_LABEL, "true"));
        page.setSpec(new SinglePage.SinglePageSpec());
        page.getSpec().setOwner("owner");
        page.getSpec().setDeleted(false);
        page.getSpec().setVisible(Post.VisibleEnum.PRIVATE);
        return page;
    }
}
