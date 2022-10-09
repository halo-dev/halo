package run.halo.app.core.extension.attachment.endpoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.attachment.Policy.PolicySpec;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.plugin.ExtensionComponentsFinder;

@ExtendWith(MockitoExtension.class)
class AttachmentEndpointTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    ExtensionComponentsFinder extensionComponentsFinder;

    @InjectMocks
    AttachmentEndpoint endpoint;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
            .apply(springSecurity())
            .build();
    }

    @Nested
    class UploadTest {

        @Test
        void shouldResponseErrorIfNotLogin() {
            webClient
                .post()
                .uri("/attachments/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .exchange()
                .expectStatus().isUnauthorized();
        }

        @Test
        void shouldResponseErrorIfNoBodyProvided() {
            webClient
                .mutateWith(mockUser("fake-user").password("fake-password"))
                .post()
                .uri("/attachments/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .exchange()
                .expectStatus().is5xxServerError();
        }

        @Test
        void shouldResponseErrorIfPolicyNameIsMissing() {
            var builder = new MultipartBodyBuilder();
            builder.part("file", "fake-file")
                .contentType(MediaType.TEXT_PLAIN)
                .filename("fake-filename");
            webClient
                .mutateWith(mockUser("fake-user").password("fake-password"))
                .post()
                .uri("/attachments/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isBadRequest();
        }

        @Test
        void shouldUploadSuccessfully() {
            var policySpec = new PolicySpec();
            policySpec.setConfigMapRef(Ref.of("fake-configmap"));
            var policyMetadata = new Metadata();
            policyMetadata.setName("fake-policy");
            var policy = new Policy();
            policy.setSpec(policySpec);
            policy.setMetadata(policyMetadata);

            var cm = new ConfigMap();
            var cmMetadata = new Metadata();
            cmMetadata.setName("fake-configmap");
            cm.setData(Map.of());

            when(client.get(Policy.class, "fake-policy")).thenReturn(Mono.just(policy));
            when(client.get(ConfigMap.class, "fake-configmap")).thenReturn(Mono.just(cm));

            var handler = mock(AttachmentHandler.class);
            var metadata = new Metadata();
            metadata.setName("fake-attachment");
            var attachment = new Attachment();
            attachment.setMetadata(metadata);

            when(handler.upload(any())).thenReturn(Mono.just(attachment));
            when(extensionComponentsFinder.getExtensions(AttachmentHandler.class)).thenReturn(
                List.of(handler));
            when(client.create(attachment)).thenReturn(Mono.just(attachment));

            var builder = new MultipartBodyBuilder();
            builder.part("policyName", "fake-policy");
            builder.part("groupName", "fake-group");
            builder.part("file", "fake-file")
                .contentType(MediaType.TEXT_PLAIN)
                .filename("fake-filename");
            webClient
                .mutateWith(mockUser("fake-user").password("fake-password"))
                .post()
                .uri("/attachments/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.metadata.name").isEqualTo("fake-attachment")
                .jsonPath("$.spec.uploadedBy.name").isEqualTo("fake-user")
                .jsonPath("$.spec.policyRef.name").isEqualTo("fake-policy")
                .jsonPath("$.spec.groupRef.name").isEqualTo("fake-group")
            ;

            verify(client).get(Policy.class, "fake-policy");
            verify(client).get(ConfigMap.class, "fake-configmap");
            verify(client).create(attachment);
            verify(extensionComponentsFinder).getExtensions(AttachmentHandler.class);
            verify(handler).upload(any());
        }
    }

}