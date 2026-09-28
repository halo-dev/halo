package run.halo.app.core.endpoint.uc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import run.halo.app.content.PostService;
import run.halo.app.core.attachment.AttachmentLister;
import run.halo.app.core.attachment.AttachmentPermalinkMatchResult;
import run.halo.app.core.attachment.AttachmentPermalinkMatcher;
import run.halo.app.core.endpoint.AttachmentHandler;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

@ExtendWith(MockitoExtension.class)
class AttachmentUcEndpointTest {

    @Mock
    AttachmentService attachmentService;

    @Mock
    AttachmentLister attachmentLister;

    @Mock
    PostService postService;

    @Mock
    SystemConfigFetcher systemConfigFetcher;

    @Mock
    AttachmentPermalinkMatcher attachmentPermalinkMatcher;

    @Mock
    ExternalUrlSupplier externalUrlSupplier;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        var attachmentHandler =
                new AttachmentHandler(attachmentService, attachmentPermalinkMatcher, externalUrlSupplier);
        var endpoint = new AttachmentUcEndpoint(
                attachmentService, attachmentLister, postService, systemConfigFetcher, attachmentHandler);
        webTestClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
                .apply(springSecurity())
                .build()
                .mutateWith(mockUser("contributor"));
    }

    @Test
    void shouldMatchPermalinks() throws Exception {
        var siteUrl = URI.create("https://www.halo.run").toURL();
        when(externalUrlSupplier.getURL(any())).thenReturn(siteUrl);
        when(attachmentPermalinkMatcher.match(List.of("/upload/halo.png"), siteUrl))
                .thenReturn(Mono.just(List.of(new AttachmentPermalinkMatchResult("/upload/halo.png", false))));

        webTestClient
                .post()
                .uri("/attachments/-/match-permalinks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {"urls":["/upload/halo.png"]}
                    """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.items[0].url")
                .isEqualTo("/upload/halo.png")
                .jsonPath("$.items[0].matched")
                .isEqualTo(false)
                .jsonPath("$.items[0].metadata")
                .doesNotExist();
    }

    @Test
    void shouldUploadUsingAttachmentSetting() {
        var config = SystemSetting.Attachment.builder()
                .uc(SystemSetting.Attachment.UploadOptions.builder()
                        .policyName("uc-policy")
                        .groupName("uc-group")
                        .build())
                .build();
        when(systemConfigFetcher.fetch(SystemSetting.Attachment.GROUP, SystemSetting.Attachment.class))
                .thenReturn(Mono.just(config));

        var attachment = new Attachment();
        var metadata = new Metadata();
        metadata.setName("uploaded-file");
        attachment.setMetadata(metadata);
        when(attachmentService.upload(eq("uc-policy"), eq("uc-group"), eq("file.png"), any(), eq(MediaType.IMAGE_PNG)))
                .thenReturn(Mono.just(attachment));
        when(attachmentService.getPermalink(attachment)).thenReturn(Mono.empty());

        var multipart = new MultipartBodyBuilder();
        multipart.part("file", "file content").filename("file.png").contentType(MediaType.IMAGE_PNG);
        webTestClient
                .post()
                .uri("/attachments/-/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipart.build()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.metadata.name")
                .isEqualTo("uploaded-file");

        verify(attachmentService)
                .upload(eq("uc-policy"), eq("uc-group"), eq("file.png"), any(), eq(MediaType.IMAGE_PNG));
    }

    @Test
    void shouldUploadFromUrlUsingAttachmentSetting() throws Exception {
        var config = SystemSetting.Attachment.builder()
                .uc(SystemSetting.Attachment.UploadOptions.builder()
                        .policyName("uc-policy")
                        .groupName("uc-group")
                        .build())
                .build();
        when(systemConfigFetcher.fetch(SystemSetting.Attachment.GROUP, SystemSetting.Attachment.class))
                .thenReturn(Mono.just(config));

        var attachment = new Attachment();
        var metadata = new Metadata();
        metadata.setName("uploaded-file");
        attachment.setMetadata(metadata);
        var url = URI.create("https://example.com/file.png").toURL();
        when(attachmentService.uploadFromUrl(url, "uc-policy", "uc-group", "file.png"))
                .thenReturn(Mono.just(attachment));

        webTestClient
                .post()
                .uri("/attachments/-/upload-from-url")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"url":"https://example.com/file.png","filename":"file.png"}
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.metadata.name")
                .isEqualTo("uploaded-file");

        verify(systemConfigFetcher, never()).fetchPost();
    }

    @Test
    void shouldUploadPostAttachmentUsingAttachmentSetting() {
        var config = SystemSetting.Attachment.builder()
                .uc(SystemSetting.Attachment.UploadOptions.builder()
                        .policyName("uc-policy")
                        .groupName("uc-group")
                        .build())
                .build();
        when(systemConfigFetcher.fetch(SystemSetting.Attachment.GROUP, SystemSetting.Attachment.class))
                .thenReturn(Mono.just(config));

        var attachment = new Attachment();
        var metadata = new Metadata();
        metadata.setName("uploaded-file");
        attachment.setMetadata(metadata);
        when(attachmentService.upload(eq("contributor"), eq("uc-policy"), eq("uc-group"), any(FilePart.class), any()))
                .thenReturn(Mono.just(attachment));

        var multipart = new MultipartBodyBuilder();
        multipart.part("file", "file content").filename("file.png").contentType(MediaType.IMAGE_PNG);
        webTestClient
                .post()
                .uri("/attachments")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipart.build()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.metadata.name")
                .isEqualTo("uploaded-file");

        verify(systemConfigFetcher, never()).fetchPost();
    }
}
