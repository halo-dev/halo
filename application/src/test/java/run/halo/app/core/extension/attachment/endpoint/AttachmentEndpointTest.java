package run.halo.app.core.extension.attachment.endpoint;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Attachment.AttachmentSpec;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.attachment.Policy.PolicySpec;
import run.halo.app.core.extension.attachment.endpoint.AttachmentEndpoint.SearchRequest;
import run.halo.app.core.extension.service.impl.DefaultAttachmentService;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.ExtensionComponentsFinder;

@ExtendWith(MockitoExtension.class)
class AttachmentEndpointTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    ExtensionComponentsFinder extensionComponentsFinder;

    AttachmentEndpoint endpoint;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        var attachmentService = new DefaultAttachmentService(client, extensionComponentsFinder);
        endpoint = new AttachmentEndpoint(attachmentService, client);
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
            .apply(springSecurity())
            .build();
    }

    @Nested
    class UploadTest {

        @Test
        void shouldResponseErrorIfNotLogin() {
            var policySpec = new PolicySpec();
            policySpec.setConfigMapName("fake-configmap");
            var policyMetadata = new Metadata();
            policyMetadata.setName("fake-policy");
            var policy = new Policy();
            policy.setSpec(policySpec);
            policy.setMetadata(policyMetadata);

            var cm = new ConfigMap();
            var cmMetadata = new Metadata();
            cmMetadata.setName("fake-configmap");
            cm.setData(Map.of());

            var handler = mock(AttachmentHandler.class);
            var metadata = new Metadata();
            metadata.setName("fake-attachment");
            var attachment = new Attachment();
            attachment.setMetadata(metadata);

            var builder = new MultipartBodyBuilder();
            builder.part("policyName", "fake-policy");
            builder.part("groupName", "fake-group");
            builder.part("file", "fake-file")
                .contentType(MediaType.TEXT_PLAIN)
                .filename("fake-filename");
            webClient
                .post()
                .uri("/attachments/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isUnauthorized();

            verify(client, never()).get(Policy.class, "fake-policy");
            verify(client, never()).get(ConfigMap.class, "fake-configmap");
            verify(client, never()).create(attachment);
            verify(extensionComponentsFinder, never()).getExtensions(AttachmentHandler.class);
            verify(handler, never()).upload(any());
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

        void prepareForUploading(Consumer<MultipartBodyBuilder> consumer) {
            var policySpec = new PolicySpec();
            policySpec.setConfigMapName("fake-configmap");
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

            consumer.accept(builder);
        }

        @Test
        void shouldUploadSuccessfully() {
            var policySpec = new PolicySpec();
            policySpec.setConfigMapName("fake-configmap");
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
                .jsonPath("$.spec.ownerName").isEqualTo("fake-user")
                .jsonPath("$.spec.policyName").isEqualTo("fake-policy")
                .jsonPath("$.spec.groupName").isEqualTo("fake-group")
            ;

            verify(client).get(Policy.class, "fake-policy");
            verify(client).get(ConfigMap.class, "fake-configmap");
            verify(client).create(attachment);
            verify(extensionComponentsFinder).getExtensions(AttachmentHandler.class);
            verify(handler).upload(any());
        }
    }

    @Nested
    class SearchTest {

        @Test
        void shouldListUngroupedAttachments() {
            when(client.list(same(Attachment.class), any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(ListResult.emptyResult()));

            webClient
                .get()
                .uri("/attachments")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("items.length()").isEqualTo(0);
        }

        @Test
        void shouldFilterWithUngrouped() {
            var httpRequest = MockServerHttpRequest.get("/attachments")
                .build();
            var exchange = new MockServerWebExchange.Builder(httpRequest)
                .build();
            MockServerRequest request = MockServerRequest.builder()
                .queryParam("ungrouped", "true")
                .queryParam("group", "halo")
                .exchange(exchange)
                .build();
            var searchRequest = new SearchRequest(request);
            var pred = searchRequest.toPredicate();
            var attachment = new Attachment();
            var spec = new AttachmentSpec();
            attachment.setSpec(spec);

            assertTrue(pred.test(attachment));

            spec.setGroupName("halo");
            assertFalse(pred.test(attachment));
        }
    }

}