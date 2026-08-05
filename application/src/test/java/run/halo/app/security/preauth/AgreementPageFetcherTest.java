package run.halo.app.security.preauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

@ExtendWith(MockitoExtension.class)
class AgreementPageFetcherTest {

    @Mock
    SystemConfigFetcher systemConfigFetcher;

    @Mock
    ReactiveExtensionClient extensionClient;

    AgreementPageFetcher fetcher;

    @BeforeEach
    void setUp() {
        fetcher = new AgreementPageFetcher(systemConfigFetcher, extensionClient);
    }

    @Test
    void shouldReturnEmptyListWhenNoAgreementPageIsConfigured() {
        var setting = new SystemSetting.User();
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));

        StepVerifier.create(fetcher.fetchAgreementPages())
                .assertNext(pages -> assertThat(pages).isEmpty())
                .verifyComplete();

        verify(extensionClient, never()).fetch(SinglePage.class, "terms");
    }

    @Test
    void shouldFetchAgreementPagesInConfiguredOrder() {
        var setting = new SystemSetting.User();
        setting.setRequiredAgreementPages(List.of("terms", "privacy"));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        when(extensionClient.fetch(SinglePage.class, "terms"))
                .thenReturn(Mono.just(page("Terms of Service", "/terms")));
        when(extensionClient.fetch(SinglePage.class, "privacy")).thenReturn(Mono.just(page("Privacy Policy", null)));

        StepVerifier.create(fetcher.fetchAgreementPages())
                .assertNext(pages -> {
                    assertThat(pages).hasSize(2);
                    assertThat(pages.get(0))
                            .containsEntry("title", "Terms of Service")
                            .containsEntry("permalink", "/terms");
                    assertThat(pages.get(1)).containsOnlyKeys("title").containsEntry("title", "Privacy Policy");
                })
                .verifyComplete();
    }

    @Test
    void shouldFailClosedWhenRequiredPageIsMissing() {
        var setting = new SystemSetting.User();
        setting.setRequiredAgreementPages(List.of("terms"));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        when(extensionClient.fetch(SinglePage.class, "terms")).thenReturn(Mono.empty());

        StepVerifier.create(fetcher.fetchAgreementPages())
                .expectErrorMatches(error -> error instanceof IllegalStateException
                        && error.getMessage().contains("terms"))
                .verify();
    }

    @Test
    void shouldPropagateExtensionClientError() {
        var setting = new SystemSetting.User();
        setting.setRequiredAgreementPages(List.of("terms"));
        var failure = new IllegalStateException("extension store unavailable");
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        when(extensionClient.fetch(SinglePage.class, "terms")).thenReturn(Mono.error(failure));

        StepVerifier.create(fetcher.fetchAgreementPages())
                .expectErrorSatisfies(error -> assertThat(error).isSameAs(failure))
                .verify();
    }

    private static SinglePage page(String title, String permalink) {
        var page = new SinglePage();
        var spec = new SinglePage.SinglePageSpec();
        spec.setTitle(title);
        page.setSpec(spec);
        if (permalink != null) {
            var status = new SinglePage.SinglePageStatus();
            status.setPermalink(permalink);
            page.setStatus(status);
        }
        return page;
    }
}
