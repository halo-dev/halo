package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.content.SinglePageService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.metrics.CounterService;
import run.halo.app.theme.finders.ContributorFinder;

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
    private SinglePageService singlePageService;

    @Mock
    private ContributorFinder contributorFinder;

    @Mock
    private CounterService counterService;

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
        when(client.get(eq(SinglePage.class), eq(fakePageName)))
            .thenReturn(Mono.just(singlePage));

        when(counterService.getByName(anyString())).thenReturn(Mono.empty());
        when(contributorFinder.getContributor(anyString())).thenReturn(Mono.empty());
        when(singlePageService.getReleaseContent(anyString())).thenReturn(Mono.empty());

        singlePageFinder.getByName(fakePageName)
            .as(StepVerifier::create)
            .consumeNextWith(page -> {
                assertThat(page.getStats()).isNotNull();
                assertThat(page.getContent()).isNotNull();
            })
            .verifyComplete();

        verify(client).get(SinglePage.class, fakePageName);
        verify(counterService).getByName(anyString());
        verify(singlePageService).getReleaseContent(anyString());
        verify(contributorFinder).getContributor(anyString());
    }
}