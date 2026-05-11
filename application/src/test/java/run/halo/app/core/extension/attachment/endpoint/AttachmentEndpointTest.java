package run.halo.app.core.extension.attachment.endpoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.AttachmentLister;
import run.halo.app.core.attachment.endpoint.AttachmentEndpoint;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;

@ExtendWith(MockitoExtension.class)
class AttachmentEndpointTest {

    @Mock
    AttachmentLister attachmentLister;

    @Mock
    AttachmentService attachmentService;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        var endpoint = new AttachmentEndpoint(attachmentService, attachmentLister);
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
                .apply(springSecurity())
                .build();
    }

    @Nested
    class UploadTest {

        @Test
        void shouldUploadCorrectly() {
            var metadata = new Metadata();
            metadata.setName("fake-attachment");
            var attachment = new Attachment();
            attachment.setMetadata(metadata);

            when(attachmentService.upload(
                            eq("fake-policy"), eq("fake-group"), eq("fake-filename"), any(), eq(MediaType.TEXT_PLAIN)))
                    .thenReturn(Mono.just(attachment));

            var builder = new MultipartBodyBuilder();
            builder.part("policyName", "fake-policy");
            builder.part("groupName", "fake-group");
            builder.part("file", "mock-file-content")
                    .contentType(MediaType.TEXT_PLAIN)
                    .filename("fake-filename");
            webClient
                    .post()
                    .uri("/attachments/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder.build())
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(Attachment.class)
                    .isEqualTo(attachment);
        }

        @Test
        void shouldResponseErrorIfNoBodyProvided() {
            webClient
                    .post()
                    .uri("/attachments/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .exchange()
                    .expectStatus()
                    .is5xxServerError();
        }
    }

    @Nested
    class SearchTest {

        @Test
        void shouldListUngroupedAttachments() {
            when(attachmentLister.listBy(any())).thenReturn(Mono.just(ListResult.emptyResult()));

            webClient
                    .get()
                    .uri("/attachments")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("items.length()")
                    .isEqualTo(0);
        }

        @Test
        void searchAttachmentWhenGroupIsEmpty() {
            when(attachmentLister.listBy(any())).thenReturn(Mono.just(ListResult.emptyResult()));

            webClient.get().uri("/attachments").exchange().expectStatus().isOk();

            verify(attachmentLister).listBy(any());
        }
    }

    @Nested
    class ExternalTransferTest {
        @Test
        void shouldResponseErrorIfNoPermalinkProvided() {
            webClient
                    .mutateWith(mockUser("fake-user").password("fake-password"))
                    .post()
                    .uri("/attachments/-/upload-from-url")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("policyName", "fake-policy"))
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void shouldTransferSuccessfully() throws MalformedURLException {
            var attachment = new Attachment();
            attachment.setMetadata(new Metadata());
            attachment.getMetadata().setName("fake-attachment");
            attachment.setSpec(new Attachment.AttachmentSpec());
            attachment.getSpec().setOwnerName("fake-user");
            attachment.getSpec().setPolicyName("fake-policy");

            var url = URI.create("http://localhost:8090/fake-url.jpg").toURL();
            when(attachmentService.uploadFromUrl(url, "fake-policy", null, null))
                    .thenReturn(Mono.just(attachment));
            var fakeValue = Map.of("policyName", "fake-policy", "url", url.toString());
            webClient
                    .post()
                    .uri("/attachments/-/upload-from-url")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(fakeValue)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.metadata.name")
                    .isEqualTo("fake-attachment")
                    .jsonPath("$.spec.ownerName")
                    .isEqualTo("fake-user")
                    .jsonPath("$.spec.policyName")
                    .isEqualTo("fake-policy");
        }
    }
}
