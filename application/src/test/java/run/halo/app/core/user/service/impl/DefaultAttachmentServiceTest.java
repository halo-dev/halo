package run.halo.app.core.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Group;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.attachment.endpoint.AttachmentHandler;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

@ExtendWith(MockitoExtension.class)
class DefaultAttachmentServiceTest {

    @Mock ReactiveExtensionClient client;

    @Mock ExtensionGetter extensionGetter;

    @Mock ExchangeFunction exchangeFunction;

    @InjectMocks DefaultAttachmentService attachmentService;

    @BeforeEach
    void setUp() {
        var webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        attachmentService.setWebClient(webClient);
    }

    @Test
    void shouldPopulateGroupWhenUploading() {
        var policy = new Policy();
        var policySpec = new Policy.PolicySpec();
        policySpec.setConfigMapName("fake-configmap");
        policy.setSpec(policySpec);

        var configMap = new ConfigMap();
        configMap.setData(Map.of());

        var group = new Group();
        group.setMetadata(new Metadata());
        group.getMetadata().setName("fake-group");

        var createdAttachment = new Attachment();
        createdAttachment.setMetadata(new Metadata());
        createdAttachment.getMetadata().setName("fake-attachment");

        var handler = mock(AttachmentHandler.class);

        when(client.get(Policy.class, "fake-policy")).thenReturn(Mono.just(policy));
        when(client.get(ConfigMap.class, "fake-configmap")).thenReturn(Mono.just(configMap));
        when(client.get(Group.class, "fake-group")).thenReturn(Mono.just(group));
        when(extensionGetter.getExtensions(AttachmentHandler.class)).thenReturn(Flux.just(handler));
        when(handler.upload(any())).thenReturn(Mono.just(createdAttachment));
        when(client.create(any(Attachment.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var filePart = mock(FilePart.class);
        StepVerifier.create(
                        attachmentService.upload(
                                "fake-user", "fake-policy", "fake-group", filePart, null))
                .assertNext(
                        (Attachment attachment) -> {
                            assertEquals("fake-attachment", attachment.getMetadata().getName());
                            assertEquals("fake-user", attachment.getSpec().getOwnerName());
                            assertEquals("fake-policy", attachment.getSpec().getPolicyName());
                            assertEquals("fake-group", attachment.getSpec().getGroupName());
                        })
                .verifyComplete();

        verify(handler)
                .upload(
                        assertArg(
                                context -> {
                                    assertEquals(policy, context.policy());
                                    assertEquals(configMap, context.configMap());
                                    assertEquals(group, context.group());
                                }));
    }

    @Test
    void shouldDelegateThumbnailLinksToAttachmentHandler() {
        var attachment = new Attachment();
        attachment.setSpec(new Attachment.AttachmentSpec());
        attachment.getSpec().setPolicyName("fake-policy");

        var policy = new Policy();
        var policySpec = new Policy.PolicySpec();
        policySpec.setConfigMapName("fake-configmap");
        policy.setSpec(policySpec);

        var configMap = new ConfigMap();
        var expected = Map.of(ThumbnailSize.M, URI.create("https://halo.run/thumbnails/m.png"));

        var handler = mock(AttachmentHandler.class);
        when(client.get(Policy.class, "fake-policy")).thenReturn(Mono.just(policy));
        when(client.get(ConfigMap.class, "fake-configmap")).thenReturn(Mono.just(configMap));
        when(extensionGetter.getExtensions(AttachmentHandler.class)).thenReturn(Flux.just(handler));
        when(handler.getThumbnailLinks(attachment, policy, configMap))
                .thenReturn(Mono.just(expected));

        StepVerifier.create(attachmentService.getThumbnailLinks(attachment))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldUploadFromUrlUsingFetchedResponseEntity() throws Exception {
        final var service = spy(attachmentService);
        var dataBuffer = mock(DataBuffer.class);
        var body = Flux.just(dataBuffer);
        var uploadedAttachment = new Attachment();
        var url = URI.create("https://example.com/assets/image.png").toURL();
        var mockResponse =
                ClientResponse.create(HttpStatus.OK)
                        .headers(
                                h -> {
                                    h.setContentType(MediaType.IMAGE_PNG);
                                    h.setContentDisposition(
                                            ContentDisposition.attachment()
                                                    .filename("remote.png")
                                                    .build());
                                })
                        .body(body)
                        .build();
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockResponse));
        when(service.upload(
                        eq("fake-policy"),
                        eq(""),
                        eq("remote.png"),
                        any(),
                        eq(MediaType.IMAGE_PNG)))
                .thenReturn(Mono.just(uploadedAttachment));

        StepVerifier.create(service.uploadFromUrl(url, "fake-policy", "", ""))
                .expectNext(uploadedAttachment)
                .verifyComplete();

        verify(exchangeFunction)
                .exchange(
                        assertArg(
                                r -> {
                                    assertEquals(url.toString(), r.url().toString());
                                    assertEquals(HttpMethod.GET, r.method());
                                }));
    }

    @Test
    void shouldRejectUploadFromUrlWhenResponseStatusIsNotSuccessful() throws Exception {
        var url = URI.create("https://example.com/file.png").toURL();
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(ClientResponse.create(HttpStatus.BAD_GATEWAY).build()));

        StepVerifier.create(attachmentService.uploadFromUrl(url, "fake-policy", "", ""))
                .consumeErrorWith(
                        error -> {
                            var exception = assertInstanceOf(ServerWebInputException.class, error);
                            assertTrue(
                                    exception
                                            .getMessage()
                                            .contains(HttpStatus.BAD_GATEWAY.toString()));
                        })
                .verify();
        verify(exchangeFunction)
                .exchange(
                        assertArg(
                                r -> {
                                    assertEquals(url.toString(), r.url().toString());
                                    assertEquals(HttpMethod.GET, r.method());
                                }));
    }

    @Test
    void shouldPreferProvidedFilenameWhenUploadingFromUrl() throws Exception {
        final var service = spy(attachmentService);
        var dataBuffer = mock(DataBuffer.class);
        var body = Flux.just(dataBuffer);
        var uploadedAttachment = new Attachment();
        var url = URI.create("https://example.com/assets/image.png").toURL();

        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(
                        Mono.just(
                                ClientResponse.create(HttpStatus.OK)
                                        .headers(h -> h.setContentType(MediaType.IMAGE_JPEG))
                                        .body(body)
                                        .build()));
        when(service.upload(
                        eq("fake-policy"),
                        eq(""),
                        eq("custom-name.jpg"),
                        any(),
                        eq(MediaType.IMAGE_JPEG)))
                .thenReturn(Mono.just(uploadedAttachment));

        StepVerifier.create(service.uploadFromUrl(url, "fake-policy", "", "custom-name.jpg"))
                .expectNext(uploadedAttachment)
                .verifyComplete();

        verify(exchangeFunction)
                .exchange(
                        assertArg(
                                r -> {
                                    assertEquals(url.toString(), r.url().toString());
                                    assertEquals(HttpMethod.GET, r.method());
                                }));
    }
}
