package run.halo.app.core.attachment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

@ExtendWith(MockitoExtension.class)
class AttachmentPermalinkMatcherTest {

    @Mock
    ReactiveExtensionClient client;

    AttachmentPermalinkMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new AttachmentPermalinkMatcher(client);
    }

    @Test
    void shouldMatchPermalinksAndPreserveInputOrder() throws Exception {
        when(client.listAll(same(Attachment.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.just(
                        attachment("https://cdn.example.com/halo.png"),
                        attachment("/upload/relative.png"),
                        attachment("https://www.halo.run/upload/absolute.png")));

        matcher.match(
                        List.of(
                                "https://cdn.example.com/halo.png",
                                "https://www.halo.run/upload/relative.png",
                                "/upload/absolute.png",
                                "https://other.example.com/missing.png"),
                        URI.create("https://www.halo.run").toURL())
                .as(StepVerifier::create)
                .assertNext(results -> {
                    org.assertj.core.api.Assertions.assertThat(results)
                            .containsExactly(
                                    new AttachmentPermalinkMatchResult("https://cdn.example.com/halo.png", true),
                                    new AttachmentPermalinkMatchResult(
                                            "https://www.halo.run/upload/relative.png", true),
                                    new AttachmentPermalinkMatchResult("/upload/absolute.png", true),
                                    new AttachmentPermalinkMatchResult("https://other.example.com/missing.png", false));
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectBlankUrls() throws Exception {
        assertThatThrownBy(() -> matcher.match(
                        List.of(" "), URI.create("https://www.halo.run").toURL()))
                .isInstanceOf(ServerWebInputException.class)
                .hasMessageContaining("url must not be blank");
    }

    @Test
    void shouldRejectUnsupportedProtocols() throws Exception {
        assertThatThrownBy(() -> matcher.match(
                        List.of("data:image/png;base64,xxx"),
                        URI.create("https://www.halo.run").toURL()))
                .isInstanceOf(ServerWebInputException.class)
                .hasMessageContaining("Unsupported URL protocol");

        assertThatThrownBy(() -> matcher.match(
                        List.of("blob:https://www.halo.run/id"),
                        URI.create("https://www.halo.run").toURL()))
                .isInstanceOf(ServerWebInputException.class)
                .hasMessageContaining("Unsupported URL protocol");

        assertThatThrownBy(() -> matcher.match(
                        List.of("file:///tmp/halo.png"),
                        URI.create("https://www.halo.run").toURL()))
                .isInstanceOf(ServerWebInputException.class)
                .hasMessageContaining("Unsupported URL protocol");

        assertThatThrownBy(() -> matcher.match(
                        List.of("ftp://example.com/halo.png"),
                        URI.create("https://www.halo.run").toURL()))
                .isInstanceOf(ServerWebInputException.class)
                .hasMessageContaining("Unsupported URL protocol");

        assertThatThrownBy(() -> matcher.match(
                        List.of("mailto:test@example.com"),
                        URI.create("https://www.halo.run").toURL()))
                .isInstanceOf(ServerWebInputException.class)
                .hasMessageContaining("Unsupported URL protocol");
    }

    @Test
    void shouldRejectEmptyRequest() throws Exception {
        assertThatThrownBy(() -> matcher.match(
                        List.of(), URI.create("https://www.halo.run").toURL()))
                .isInstanceOf(ServerWebInputException.class)
                .hasMessageContaining("urls must not be empty");
    }

    private static Attachment attachment(String permalink) {
        var attachment = new Attachment();
        attachment.setMetadata(new Metadata());
        attachment.getMetadata().setName(permalink);
        attachment.setStatus(new Attachment.AttachmentStatus());
        attachment.getStatus().setPermalink(permalink);
        return attachment;
    }
}
