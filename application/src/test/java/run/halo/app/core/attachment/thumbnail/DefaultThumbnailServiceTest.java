package run.halo.app.core.attachment.thumbnail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Cache;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.filter.reactive.ServerWebExchangeContextFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.properties.AttachmentProperties;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.HaloUtils;

@ExtendWith(MockitoExtension.class)
class DefaultThumbnailServiceTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    ExternalUrlSupplier externalUrlSupplier;

    @Mock
    HaloProperties haloProperties;

    @InjectMocks
    DefaultThumbnailService thumbnailService;

    AttachmentProperties attachmentProperties;

    @BeforeEach
    void setUp() {
        attachmentProperties = new AttachmentProperties();
        lenient().when(haloProperties.getAttachment()).thenReturn(attachmentProperties);
    }

    @Test
    void shouldGetThumbnailDirectlyIfPermalinkIsRelative() {
        thumbnailService
                .get(URI.create("/images/fake.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("/images/fake.png?width=800"))
                .verifyComplete();
    }

    @Test
    void shouldGetEmptyThumbnailIfThumbnailIsDisabled() {
        attachmentProperties.getThumbnail().setDisabled(true);

        thumbnailService
                .get(URI.create("/images/fake.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldGetThumbnailDirectlyIfPermalinkContainsSpecialChars() {
        thumbnailService
                .get(URI.create("/images/中文.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("/images/%E4%B8%AD%E6%96%87.png?width=800"))
                .verifyComplete();

        thumbnailService
                .get(URI.create("/images/space%20space.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("/images/space%20space.png?width=800"))
                .verifyComplete();

        thumbnailService
                .get(URI.create("/images/percent%2f.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("/images/percent%2f.png?width=800"))
                .verifyComplete();

        thumbnailService
                .get(URI.create("/images/a%23b%3Fc%26d%3De%2Bf.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("/images/a%23b%3Fc%26d%3De%2Bf.png?width=800"))
                .verifyComplete();

        thumbnailService
                .get(URI.create("/images/100%25.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("/images/100%25.png?width=800"))
                .verifyComplete();
    }

    @Test
    void shouldGetThumbnailDirectlyIfPermalinkIsInSite() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        thumbnailService
                .get(URI.create("https://www.halo.run/images/fake.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("https://www.halo.run/images/fake.png?width=800"))
                .verifyComplete();
    }

    @Test
    void shouldGetEmptyThumbnailIfNoAttachmentsFound() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.empty());
        thumbnailService
                .get(URI.create("https://fake.halo.run/fake.png"))
                .as(StepVerifier::create)
                .expectNext(Map.of())
                .verifyComplete();

        // Only invoke once due to caching
        verify(client).listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class));
    }

    @Test
    void shouldGetThumbnailsIfAttachmentsFound() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.just(
                        createAttachment(
                                "fake-png", "https://fake.halo.run/fake.png", Map.of("s", "/fake.png?width=400")),
                        createAttachment(
                                "fake-png", "https://fake.halo.run/fake.png", Map.of("m", "/fake.png?width=800"))));

        thumbnailService
                .get(URI.create("https://fake.halo.run/fake.png"))
                .as(StepVerifier::create)
                .expectNext(Map.of(ThumbnailSize.S, URI.create("/fake.png?width=400")))
                .verifyComplete();

        // Only invoke once due to caching
        verify(client).listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class));
    }

    @Test
    void shouldRedirectRelativeUriToWidthQuery() {
        thumbnailService
                .getSafeRedirectUri(URI.create("/upload/a.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("/upload/a.png?width=800"))
                .verifyComplete();
    }

    @Test
    void shouldRedirectRelativeUriWithLiteralPercentInFileName() {
        thumbnailService
                .getSafeRedirectUri(URI.create("/upload/100%25.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("/upload/100%25.png?width=800"))
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/upload/%E4%B8%AD%E6%96%87%20%E6%96%87%E4%BB%B6.png",
                "/upload/emoji-%F0%9F%98%80.png",
                "/upload/a%23b%3Fc%26d%3De%2Bf.png",
                "/upload/percent%2Fslash.png",
                "/upload/100%25.png",
                "/upload/space%20name.png",
                "/upload/colon%3Aname.png",
                "/upload/tilde%7Ename.png"
            })
    void shouldRedirectComplexRelativeUriToWidthQuery(String rawUri) {
        thumbnailService
                .getSafeRedirectUri(URI.create(rawUri), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create(rawUri + "?width=800"))
                .verifyComplete();
    }

    @Test
    void shouldRedirectToStoredComplexRelativeThumbnail() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        var thumbnail = "/upload/%E4%B8%AD%E6%96%87%20%E6%96%87%E4%BB%B6.png?width=800";
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.just(
                        createAttachment("cdn-png", "https://cdn.example.com/a.png", Map.of("m", thumbnail))));

        thumbnailService
                .getSafeRedirectUri(URI.create("https://cdn.example.com/a.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create(thumbnail))
                .verifyComplete();
    }

    @Test
    void shouldRedirectToStoredRelativeThumbnailWithLiteralPercentInFileName() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.just(createAttachment(
                        "cdn-png", "https://cdn.example.com/a.png", Map.of("m", "/upload/100%25.png?width=800"))));

        thumbnailService
                .getSafeRedirectUri(URI.create("https://cdn.example.com/a.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("/upload/100%25.png?width=800"))
                .verifyComplete();
    }

    @Test
    void shouldRedirectSiteLocalAbsoluteUriToWidthQuery() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        thumbnailService
                .getSafeRedirectUri(URI.create("https://www.halo.run/upload/a.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("https://www.halo.run/upload/a.png?width=800"))
                .verifyComplete();
    }

    @Test
    void shouldRedirectKnownExternalUriToStoredThumbnail() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.just(createAttachment(
                        "cdn-png",
                        "https://cdn.example.com/a.png",
                        Map.of("m", "https://cdn.example.com/a.png?imageView2/0/w/800"))));

        thumbnailService
                .getSafeRedirectUri(URI.create("https://cdn.example.com/a.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("https://cdn.example.com/a.png?imageView2/0/w/800"))
                .verifyComplete();
    }

    @Test
    void shouldRedirectKnownExternalUriToSiteLocalStoredThumbnail() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.just(createAttachment(
                        "cdn-png", "https://cdn.example.com/a.png", Map.of("m", "/fake.png?width=800"))));

        thumbnailService
                .getSafeRedirectUri(URI.create("https://cdn.example.com/a.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("/fake.png?width=800"))
                .verifyComplete();
    }

    @Test
    void shouldRedirectKnownExternalUriWithoutThumbnailsToStoredPermalink() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.just(createAttachment("cdn-png", "https://cdn.example.com/a.png", Map.of())));

        thumbnailService
                .getSafeRedirectUri(URI.create("https://cdn.example.com/a.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("https://cdn.example.com/a.png"))
                .verifyComplete();
    }

    @Test
    void shouldNotRedirectUnknownExternalUri() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.empty());

        thumbnailService
                .getSafeRedirectUri(URI.create("https://evil.example.com/phish"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldNotTrustRequestHostForSiteLocalCheck() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("https://www.halo.run/upload/a.png").build());
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.empty());

        StepVerifier.create(thumbnailService
                        .getSafeRedirectUri(URI.create("https://www.halo.run/upload/a.png"), ThumbnailSize.M)
                        .contextWrite(context ->
                                context.put(ServerWebExchangeContextFilter.EXCHANGE_CONTEXT_ATTRIBUTE, exchange)))
                .verifyComplete();

        // Without a configured external URL, the request-derived host must not make an absolute URI site-local.
        verify(externalUrlSupplier, Mockito.never()).getURL(Mockito.any());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/%5Cevil.example/a.png",
                "/%5cevil.example/a.png",
                "/%255Cevil.example/a.png",
                "/%25255Cevil.example/a.png",
                "%2F%2Fevil.example/a.png",
                "/%2Fevil.example/a.png",
                "%252F%252Fevil.example/a.png"
            })
    void shouldNotTreatSmuggledRelativeUriAsSiteLocal(String rawUri) {
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.empty());

        thumbnailService
                .getSafeRedirectUri(URI.create(rawUri), ThumbnailSize.M)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "%68%74%74%70%73%3A%2F%2Fevil.example/a.png",
                "https%3A%2F%2Fevil.example/a.png",
                "https%3A%2Fevil.example/a.png",
                "javascript%3Aalert(1)"
            })
    void shouldNotTreatEncodedSchemeAsSiteLocal(String rawUri) {
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.empty());

        thumbnailService
                .getSafeRedirectUri(URI.create(rawUri), ThumbnailSize.M)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "%20%2F%2Fevil.example/a.png",
                "%09%09%2F%2Fevil.example/a.png",
                "%00%2F%2Fevil.example/a.png",
                "%20https%3A%2F%2Fevil.example/a.png",
                "%20javascript%3Aalert(1)",
                "%2F%0D%0A%2Fevil.example/a.png",
                "h%09ttp%3A%2F%2Fevil.example/a.png"
            })
    void shouldNotTreatControlCharacterSmugglingAsSiteLocal(String rawUri) {
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.empty());

        thumbnailService
                .getSafeRedirectUri(URI.create(rawUri), ThumbnailSize.M)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldIgnoreQueryResultThatRacedWithRenameBeforeItWasIndexed() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        var oldPermalink = "https://cdn.example.com/old.png";
        var newPermalink = "https://cdn.example.com/new.png";
        var stale = createAttachment("cdn-png", oldPermalink, Map.of("m", oldPermalink + "?w=800"));
        var renamed = createAttachment("cdn-png", newPermalink, Map.of("m", newPermalink + "?w=800"));
        var queryResult = Sinks.<Attachment>one();
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(queryResult.asMono().flux(), Flux.empty());

        StepVerifier.create(thumbnailService.getSafeRedirectUri(URI.create(oldPermalink), ThumbnailSize.M))
                .then(() -> {
                    // The rename lands before the stale query result is applied, and the old permalink was never
                    // indexed, so its version could not be bumped by the change event.
                    thumbnailService.updateCache(renamed);
                    queryResult.tryEmitValue(stale);
                })
                .verifyComplete();

        // The stale snapshot must not bring the old permalink back, and the new permalink stays usable.
        thumbnailService
                .getSafeRedirectUri(URI.create(oldPermalink), ThumbnailSize.M)
                .as(StepVerifier::create)
                .verifyComplete();
        thumbnailService
                .getSafeRedirectUri(URI.create(newPermalink), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create(newPermalink + "?w=800"))
                .verifyComplete();
    }

    @Test
    void shouldIgnoreStaleQueryResultWhenAttachmentWasRenamedAndDeletedBeforeItWasIndexed()
            throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        var oldPermalink = "https://cdn.example.com/old.png";
        var newPermalink = "https://cdn.example.com/new.png";
        var stale = createAttachment("cdn-png", oldPermalink, Map.of("m", oldPermalink + "?w=800"));
        var renamedAndDeleted = createAttachment("cdn-png", newPermalink, Map.of("m", newPermalink + "?w=800"));
        renamedAndDeleted.getMetadata().setDeletionTimestamp(Instant.now());
        var queryResult = Sinks.<Attachment>one();
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(queryResult.asMono().flux(), Flux.empty());

        StepVerifier.create(thumbnailService.getSafeRedirectUri(URI.create(oldPermalink), ThumbnailSize.M))
                .then(() -> {
                    // The old permalink was never indexed or versioned. The attachment's change sequence must
                    // reject this snapshot even though deletion leaves no reverse association behind.
                    thumbnailService.updateCache(renamedAndDeleted);
                    queryResult.tryEmitValue(stale);
                })
                .verifyComplete();

        // The stale snapshot must not bring the deleted attachment's old permalink back.
        thumbnailService
                .getSafeRedirectUri(URI.create(oldPermalink), ThumbnailSize.M)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "http:evil.example",
                "http:/evil.example/a.png",
                "https:/evil.example/a.png",
                "https:///evil.example/a.png",
                "///evil.example/a.png",
                "////evil.example/a.png",
                "javascript:alert(1)",
                "data:text/plain,evil"
            })
    void shouldNotRedirectUrisWithoutTrustedAuthority(String rawUri) throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.empty());

        thumbnailService
                .getSafeRedirectUri(HaloUtils.safeToUri(rawUri), ThumbnailSize.M)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldRedirectSiteLocalUriWithNormalizedAuthority() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());

        thumbnailService
                .getSafeRedirectUri(HaloUtils.safeToUri("https://WWW.HALO.RUN:443/upload/a.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNextMatches(uri -> "www.halo.run".equalsIgnoreCase(uri.getHost())
                        && uri.getPort() == 443
                        && "/upload/a.png".equals(uri.getPath())
                        && "width=800".equals(uri.getQuery()))
                .verifyComplete();
    }

    @Test
    void shouldNotRedirectSameHostWithDifferentPort() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.empty());

        thumbnailService
                .getSafeRedirectUri(URI.create("https://www.halo.run:8443/upload/a.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldNotRedirectToNonHttpStoredPermalink() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.just(createAttachment("evil", "javascript:alert(1)", Map.of())));

        thumbnailService
                .getSafeRedirectUri(URI.create("javascript:alert(1)"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldIgnoreQueryResultThatRacedWithDeletion() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        var permalink = "https://cdn.example.com/a.png";
        var live = createAttachment("live", permalink, Map.of("m", permalink + "?w=800"));
        var deleted = createAttachment("live", permalink, Map.of("m", permalink + "?w=800"));
        deleted.getMetadata().setDeletionTimestamp(Instant.now());
        var queryResult = Sinks.<Attachment>one();
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(queryResult.asMono().flux(), Flux.empty());

        StepVerifier.create(thumbnailService.getSafeRedirectUri(URI.create(permalink), ThumbnailSize.M))
                .then(() -> {
                    // The attachment is deleted while the query is still in flight.
                    thumbnailService.updateCache(deleted);
                    queryResult.tryEmitValue(live);
                })
                .verifyComplete();

        // The stale result must not be cached either.
        thumbnailService
                .getSafeRedirectUri(URI.create(permalink), ThumbnailSize.M)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldTrustNewPermalinkSeenBeforeItsChangeEvent() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        var oldPermalink = "https://cdn.example.com/old.png";
        var newPermalink = "https://cdn.example.com/new.png";
        var attachment = createAttachment("cdn-png", oldPermalink, Map.of("m", oldPermalink + "?w=800"));
        thumbnailService.updateCache(attachment);

        // The permalink changed in the database, but the change event has not been delivered yet (or, for a
        // status-only update, will never be delivered). The index entry predates the query, so the newer database
        // snapshot is trusted and repairs the index.
        attachment.getStatus().setPermalink(newPermalink);
        attachment.getStatus().setThumbnails(Map.of("m", newPermalink + "?w=800"));
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.just(attachment));

        thumbnailService
                .getSafeRedirectUri(URI.create(newPermalink), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create(newPermalink + "?w=800"))
                .verifyComplete();

        // The old permalink was dropped when the index was repaired.
        thumbnailService
                .getSafeRedirectUri(URI.create(oldPermalink), ThumbnailSize.M)
                .as(StepVerifier::create)
                .verifyComplete();

        // The pending change event is idempotent.
        thumbnailService.updateCache(attachment);
        thumbnailService
                .getSafeRedirectUri(URI.create(newPermalink), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create(newPermalink + "?w=800"))
                .verifyComplete();
    }

    @Test
    void shouldRestorePermalinkAfterItWasClearedWithoutAnotherChangeEvent() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        var permalink = "https://cdn.example.com/a.png";
        var attachment = createAttachment("cdn-png", permalink, Map.of("m", permalink + "?w=800"));
        thumbnailService.updateCache(attachment);

        // The permalink is cleared and its reverse association is removed. A later status-only update restores the
        // same permalink without publishing another event.
        thumbnailService.updateCache(createAttachment("cdn-png", null, Map.of()));

        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.just(attachment));

        thumbnailService
                .getSafeRedirectUri(URI.create(permalink), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create(permalink + "?w=800"))
                .verifyComplete();

        // The association was restored, so the next lookup is served without another database query.
        thumbnailService
                .getSafeRedirectUri(URI.create(permalink), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create(permalink + "?w=800"))
                .verifyComplete();
        verify(client, Mockito.times(1)).listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class));
    }

    @Test
    void shouldNotRetryLookupThatRacedWithChangeInOtherStripes() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        var permalink = "https://cdn.example.com/a.png";
        var live = createAttachment("live", permalink, Map.of("m", permalink + "?w=800"));
        var unrelated = createAttachment("other", "https://cdn.example.com/b.png", Map.of());
        var queryResult = Sinks.<Attachment>one();
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(queryResult.asMono().flux(), Flux.just(live));

        StepVerifier.create(thumbnailService.getSafeRedirectUri(URI.create(permalink), ThumbnailSize.M))
                .then(() -> {
                    // An unrelated attachment changes while the query is still in flight.
                    thumbnailService.updateCache(unrelated);
                    queryResult.tryEmitValue(live);
                })
                .expectNext(URI.create(permalink + "?w=800"))
                .verifyComplete();

        // The change must not invalidate an in-flight lookup of another permalink.
        verify(client, Mockito.times(1)).listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class));
    }

    @Test
    void shouldResolveOtherLiveAttachmentWithSamePermalinkAfterDeletion() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        var permalink = "https://cdn.example.com/a.png";
        var live = createAttachment("live", permalink, Map.of("m", permalink + "?w=800"));
        var deleted = createAttachment("deleted", permalink, Map.of("m", permalink + "?w=800"));
        deleted.getMetadata().setDeletionTimestamp(Instant.now());
        thumbnailService.updateCache(live);
        thumbnailService.updateCache(deleted);

        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.just(live));

        thumbnailService
                .getSafeRedirectUri(URI.create(permalink), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create(permalink + "?w=800"))
                .verifyComplete();
    }

    @Test
    void shouldRedirectToStoredPermalinkWhenThumbnailsDisabled() throws MalformedURLException {
        attachmentProperties.getThumbnail().setDisabled(true);
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.just(createAttachment(
                        "cdn-png",
                        "https://cdn.example.com/a.png",
                        Map.of("m", "https://cdn.example.com/a.png?w=800"))));

        thumbnailService
                .getSafeRedirectUri(URI.create("https://cdn.example.com/a.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("https://cdn.example.com/a.png"))
                .verifyComplete();
    }

    @Test
    void shouldInvalidatePreviousPermalinkWhenAttachmentPermalinkChanges() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        var attachment = createAttachment(
                "cdn-png", "https://cdn.example.com/old.png", Map.of("m", "https://cdn.example.com/old.png?w=800"));
        thumbnailService.updateCache(attachment);

        thumbnailService
                .getSafeRedirectUri(URI.create("https://cdn.example.com/old.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("https://cdn.example.com/old.png?w=800"))
                .verifyComplete();

        attachment.getStatus().setPermalink("https://cdn.example.com/new.png");
        attachment.getStatus().setThumbnails(Map.of("m", "https://cdn.example.com/new.png?w=800"));
        thumbnailService.updateCache(attachment);

        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.empty());
        thumbnailService
                .getSafeRedirectUri(URI.create("https://cdn.example.com/old.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .verifyComplete();

        thumbnailService
                .getSafeRedirectUri(URI.create("https://cdn.example.com/new.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create("https://cdn.example.com/new.png?w=800"))
                .verifyComplete();
    }

    @Test
    void shouldNotRedirectDeletedAttachment() throws MalformedURLException {
        when(externalUrlSupplier.getRaw())
                .thenReturn(URI.create("https://www.halo.run").toURL());
        var attachment = createAttachment(
                "cdn-png", "https://cdn.example.com/a.png", Map.of("m", "https://cdn.example.com/a.png?w=800"));
        attachment.getMetadata().setDeletionTimestamp(Instant.now());
        thumbnailService.updateCache(attachment);

        Mockito.when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(Flux.empty());
        thumbnailService
                .getSafeRedirectUri(URI.create("https://cdn.example.com/a.png"), ThumbnailSize.M)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldReleaseReverseIndexAfterDeletion() {
        var attachment = createAttachment("file", "https://cdn.example.com/a.png", Map.of());
        thumbnailService.updateCache(attachment);
        assertThat(permalinkIndex()).containsOnlyKeys("file");

        attachment.getMetadata().setDeletionTimestamp(Instant.now());
        thumbnailService.updateCache(attachment);

        assertThat(permalinkIndex()).isEmpty();
        assertThat(thumbnailCache().asMap()).isEmpty();
    }

    @Test
    void shouldReleaseReverseIndexAfterPermalinkIsCleared() {
        thumbnailService.updateCache(createAttachment("file", "https://cdn.example.com/a.png", Map.of()));

        thumbnailService.updateCache(createAttachment("file", null, Map.of()));

        assertThat(permalinkIndex()).isEmpty();
        assertThat(thumbnailCache().asMap()).isEmpty();
    }

    @Test
    void shouldReleaseReverseIndexAfterExpiration() {
        thumbnailService.updateCache(createAttachment("file", "https://cdn.example.com/a.png", Map.of()));
        assertThat(permalinkIndex()).containsOnlyKeys("file");

        var cache = thumbnailCache();
        cache.policy().expireAfterWrite().orElseThrow().setExpiresAfter(Duration.ZERO);
        cache.cleanUp();

        assertThat(cache.asMap()).isEmpty();
        assertThat(permalinkIndex()).isEmpty();
    }

    @Test
    void shouldRetainReverseIndexOnlyForCachedEntriesAfterEviction() {
        var cache = thumbnailCache();
        cache.policy().eviction().orElseThrow().setMaximum(2);
        for (var i = 0; i < 6; i++) {
            thumbnailService.updateCache(
                    createAttachment("file-" + i, "https://cdn.example.com/" + i + ".png", Map.of()));
        }
        cache.cleanUp();

        assertThat(cache.asMap()).hasSize(2);
        assertThat(permalinkIndex()).hasSize(2);
        for (var i = 0; i < 6; i++) {
            assertThat(permalinkIndex().containsKey("file-" + i))
                    .isEqualTo(cache.getIfPresent("https://cdn.example.com/" + i + ".png") != null);
        }
    }

    @Test
    void shouldReleasePreviousOwnerWhenSharedPermalinkIsReplaced() {
        var permalink = "https://cdn.example.com/shared.png";
        thumbnailService.updateCache(createAttachment("first", permalink, Map.of()));
        thumbnailService.updateCache(createAttachment("second", permalink, Map.of()));

        assertThat(thumbnailCache().asMap()).hasSize(1);
        assertThat(permalinkIndex()).containsOnlyKeys("second");
    }

    @Test
    void shouldKeepNewAssociationWhenIdenticalCacheEntryIsReplaced() {
        var oldPermalink = "https://cdn.example.com/old.png";
        var newPermalink = "https://cdn.example.com/new.png";
        thumbnailService.updateCache(createAttachment("file", oldPermalink, Map.of()));
        thumbnailService.updateCache(createAttachment("file", oldPermalink, Map.of()));
        assertThat(permalinkIndex()).containsOnlyKeys("file");

        thumbnailService.updateCache(createAttachment("file", newPermalink, Map.of()));

        assertThat(thumbnailCache().asMap()).containsOnlyKeys(newPermalink);
        assertThat(permalinkIndex()).containsOnlyKeys("file");
    }

    @Test
    void shouldInvalidateOriginalCacheKeyWhenUnicodePermalinkChanges() {
        var oldPermalink = "https://cdn.example.com/图片.png";
        var newPermalink = "https://cdn.example.com/new.png";
        thumbnailService.updateCache(createAttachment("file", oldPermalink, Map.of()));
        assertThat(thumbnailCache().asMap()).containsOnlyKeys(oldPermalink);

        thumbnailService.updateCache(createAttachment("file", newPermalink, Map.of()));

        assertThat(thumbnailCache().asMap()).containsOnlyKeys(newPermalink);
        assertThat(permalinkIndex()).containsOnlyKeys("file");
    }

    @Test
    void shouldNotCacheMissAfterAttachmentStripeCollisionsExhaustRetries() {
        // These names have identical hashes, so they collide regardless of the number of stripes.
        assertThat("Aa".hashCode()).isEqualTo("BB".hashCode());
        var permalink = "https://cdn.example.com/a.png";
        var attachment = createAttachment("Aa", permalink, Map.of("m", permalink));
        var unrelated = createAttachment("BB", "https://cdn.example.com/b.png", Map.of());
        var firstQuery = Sinks.<Attachment>one();
        var secondQuery = Sinks.<Attachment>one();
        when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(firstQuery.asMono().flux(), secondQuery.asMono().flux(), Flux.just(attachment));

        StepVerifier.create(thumbnailService.get(URI.create(permalink)))
                .then(() -> {
                    thumbnailService.updateCache(unrelated);
                    firstQuery.tryEmitValue(attachment);
                })
                .then(() -> {
                    thumbnailService.updateCache(unrelated);
                    secondQuery.tryEmitValue(attachment);
                })
                .expectNext(Map.of())
                .verifyComplete();

        assertThat(thumbnailCache().asMap()).doesNotContainKey(permalink);
        thumbnailService
                .get(URI.create(permalink), ThumbnailSize.M)
                .as(StepVerifier::create)
                .expectNext(URI.create(permalink))
                .verifyComplete();
        verify(client, Mockito.times(3)).listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class));
    }

    @Test
    void shouldDiscardEarlierQueryAfterRenamedEntryExpires() {
        var oldPermalink = "https://cdn.example.com/old.png";
        var newPermalink = "https://cdn.example.com/new.png";
        var queryResult = Sinks.<Attachment>one();
        when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(queryResult.asMono().flux(), Flux.empty());

        StepVerifier.create(thumbnailService.get(URI.create(oldPermalink)))
                .then(() -> {
                    thumbnailService.updateCache(createAttachment("file", newPermalink, Map.of()));
                    var expiration =
                            thumbnailCache().policy().expireAfterWrite().orElseThrow();
                    expiration.setExpiresAfter(Duration.ZERO);
                    thumbnailCache().cleanUp();
                    assertThat(permalinkIndex()).isEmpty();
                    expiration.setExpiresAfter(Duration.ofHours(1));
                    queryResult.tryEmitValue(createAttachment("file", oldPermalink, Map.of("m", oldPermalink)));
                })
                .expectNext(Map.of())
                .verifyComplete();

        assertThat(permalinkIndex()).isEmpty();
        verify(client, Mockito.times(2)).listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class));
    }

    @Test
    void shouldDiscardEarlierQueryAfterNewerSnapshotIsCachedAndEvicted() {
        var oldPermalink = "https://cdn.example.com/old.png";
        var newPermalink = "https://cdn.example.com/new.png";
        var queryResult = Sinks.<Attachment>one();
        when(client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class)))
                .thenReturn(
                        queryResult.asMono().flux(),
                        Flux.just(createAttachment("file", newPermalink, Map.of("m", newPermalink))),
                        Flux.empty());

        StepVerifier.create(thumbnailService.get(URI.create(oldPermalink)))
                .then(() -> {
                    // A newer database snapshot repairs the permalink without any change event.
                    thumbnailService
                            .get(URI.create(newPermalink), ThumbnailSize.M)
                            .as(StepVerifier::create)
                            .expectNext(URI.create(newPermalink))
                            .verifyComplete();
                    assertThat(permalinkIndex()).containsOnlyKeys("file");
                    thumbnailCache().policy().eviction().orElseThrow().setMaximum(0);
                    thumbnailCache().cleanUp();
                    assertThat(permalinkIndex()).isEmpty();
                    thumbnailCache().policy().eviction().orElseThrow().setMaximum(10_000);
                    queryResult.tryEmitValue(createAttachment("file", oldPermalink, Map.of("m", oldPermalink)));
                })
                .expectNext(Map.of())
                .verifyComplete();

        assertThat(permalinkIndex()).isEmpty();
        verify(client, Mockito.times(3)).listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class));
    }

    @SuppressWarnings("unchecked")
    private Cache<String, ?> thumbnailCache() {
        return (Cache<String, ?>) ReflectionTestUtils.getField(thumbnailService, "thumbnailCache");
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> permalinkIndex() {
        return (Map<String, ?>) ReflectionTestUtils.getField(thumbnailService, "attachmentPermalinkIndex");
    }

    Attachment createAttachment(String name, String permalink, Map<String, String> thumbnails) {
        var attachment = new Attachment();
        attachment.setMetadata(new Metadata());
        attachment.getMetadata().setName(name);
        attachment.setSpec(new Attachment.AttachmentSpec());
        attachment.setStatus(new Attachment.AttachmentStatus());
        attachment.getStatus().setPermalink(permalink);
        attachment.getStatus().setThumbnails(thumbnails);
        return attachment;
    }
}
