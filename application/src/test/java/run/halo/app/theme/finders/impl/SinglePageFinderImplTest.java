package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private SinglePageFinderImpl singlePageFinder;

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

    @Test
    void contentOfPrivatePageIsVisibleOnlyToItsOwner() {
        var page = privatePage();
        when(client.get(SinglePage.class, "private")).thenReturn(Mono.just(page));

        StepVerifier.create(singlePageFinder.content("private")).verifyComplete();
        var visitor = new TestingAuthenticationToken("visitor", "");
        StepVerifier.create(singlePageFinder
                        .content("private")
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(visitor)))
                .verifyComplete();
        verifyNoInteractions(singlePageConversionService);

        var content = ContentVo.builder().content("released body").build();
        when(singlePageConversionService.getContent(page)).thenReturn(Mono.just(content));
        var owner = new TestingAuthenticationToken("owner", "");
        StepVerifier.create(singlePageFinder
                        .content("private")
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(owner)))
                .expectNext(content)
                .verifyComplete();
    }

    @Test
    void anonymousContentReadsOnlyReleasedNonDeletedPages() {
        var page = privatePage();
        page.getSpec().setVisible(Post.VisibleEnum.PUBLIC);
        when(client.get(SinglePage.class, "private")).thenReturn(Mono.just(page));

        page.getMetadata().setLabels(Map.of());
        StepVerifier.create(singlePageFinder.content("private")).verifyComplete();
        page.getMetadata().setLabels(Map.of(SinglePage.PUBLISHED_LABEL, "true"));
        page.getSpec().setDeleted(true);
        StepVerifier.create(singlePageFinder.content("private")).verifyComplete();
        verifyNoInteractions(singlePageConversionService);

        page.getSpec().setDeleted(false);
        var content = ContentVo.builder().content("public body").build();
        when(singlePageConversionService.getContent(page)).thenReturn(Mono.just(content));
        StepVerifier.create(singlePageFinder.content("private"))
                .expectNext(content)
                .verifyComplete();
    }

    @Test
    void deletingPublishedPageCannotBeReadInEitherLanguage() {
        var page = privatePage();
        page.getSpec().setVisible(Post.VisibleEnum.PUBLIC);
        page.getMetadata().setDeletionTimestamp(Instant.parse("2026-11-16T00:00:00Z"));
        when(client.get(SinglePage.class, "private")).thenReturn(Mono.just(page));
        var localization =
                new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);

        StepVerifier.create(singlePageFinder.getByName("private")).verifyComplete();
        StepVerifier.create(singlePageFinder.content("private")).verifyComplete();
        StepVerifier.create(singlePageFinder
                        .getByName("private")
                        .contextWrite(context -> context.put(LocalizationContext.class, localization)))
                .verifyComplete();
        StepVerifier.create(singlePageFinder
                        .content("private")
                        .contextWrite(context -> context.put(LocalizationContext.class, localization)))
                .verifyComplete();
        verifyNoInteractions(singlePageConversionService);
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
