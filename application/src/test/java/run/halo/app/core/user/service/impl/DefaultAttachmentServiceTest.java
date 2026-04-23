package run.halo.app.core.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
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
import run.halo.app.infra.ReactiveUrlDataBufferFetcher;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

@ExtendWith(MockitoExtension.class)
class DefaultAttachmentServiceTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    ExtensionGetter extensionGetter;

    @Mock
    ReactiveUrlDataBufferFetcher dataBufferFetcher;

    @InjectMocks
    DefaultAttachmentService attachmentService;

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
                    "fake-user",
                    "fake-policy",
                    "fake-group",
                    filePart,
                    null
                )
            )
            .assertNext((Attachment attachment) -> {
                assertEquals("fake-attachment", attachment.getMetadata().getName());
                assertEquals("fake-user", attachment.getSpec().getOwnerName());
                assertEquals("fake-policy", attachment.getSpec().getPolicyName());
                assertEquals("fake-group", attachment.getSpec().getGroupName());
            })
            .verifyComplete();

        verify(handler).upload(assertArg(context -> {
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
        var headers = new HttpHeaders();
        headers.setContentDisposition(
            ContentDisposition.attachment().filename("remote.png").build());
        headers.setContentType(MediaType.IMAGE_PNG);
        var response = ResponseEntity.ok().headers(headers).body(body);
        var uploadedAttachment = new Attachment();
        URL url = URI.create("https://example.com/assets/image.png").toURL();

        when(dataBufferFetcher.fetchResponseEntity(
            URI.create("https://example.com/assets/image.png")))
            .thenReturn(Mono.just(response));
        doReturn(Mono.just(uploadedAttachment)).when(service)
            .upload("fake-policy", "", "remote.png", body, MediaType.IMAGE_PNG);

        StepVerifier.create(service.uploadFromUrl(url, "fake-policy", "", ""))
            .expectNext(uploadedAttachment)
            .verifyComplete();
    }

    @Test
    void shouldRejectUploadFromUrlWhenResponseStatusIsNotSuccessful() throws Exception {
        URL url = URI.create("https://example.com/file.png").toURL();
        var response = ResponseEntity.status(HttpStatus.BAD_GATEWAY).<Flux<DataBuffer>>build();
        when(dataBufferFetcher.fetchResponseEntity(URI.create("https://example.com/file.png")))
            .thenReturn(Mono.just(response));

        StepVerifier.create(attachmentService.uploadFromUrl(url, "fake-policy", "", ""))
            .consumeErrorWith(error -> {
                var exception = assertInstanceOf(ServerWebInputException.class, error);
                assertEquals(
                    "Failed to fetch the content from the external URL due to non-successful "
                        + "response status.",
                    exception.getReason()
                );
            })
            .verify();
    }

    @Test
    void shouldRejectUploadFromUrlWhenResponseBodyIsEmpty() throws Exception {
        URL url = URI.create("https://example.com/file.png").toURL();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        var response = ResponseEntity.ok().headers(headers).<Flux<DataBuffer>>build();
        when(dataBufferFetcher.fetchResponseEntity(URI.create("https://example.com/file.png")))
            .thenReturn(Mono.just(response));

        StepVerifier.create(attachmentService.uploadFromUrl(url, "fake-policy", "", ""))
            .consumeErrorWith(error -> {
                var exception = assertInstanceOf(
                    ServerWebInputException.class,
                    error
                );
                assertEquals(
                    "Failed to fetch the content from the external URL due to empty response body.",
                    exception.getReason()
                );
            })
            .verify();
    }

    @Test
    void shouldPreferProvidedFilenameWhenUploadingFromUrl() throws Exception {
        final var service = spy(attachmentService);
        var dataBuffer = mock(DataBuffer.class);
        var body = Flux.just(dataBuffer);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        var response = ResponseEntity.ok().headers(headers).body(body);
        var uploadedAttachment = new Attachment();
        URL url = URI.create("https://example.com/assets/image.png").toURL();

        when(dataBufferFetcher.fetchResponseEntity(
            URI.create("https://example.com/assets/image.png")))
            .thenReturn(Mono.just(response));
        doReturn(Mono.just(uploadedAttachment)).when(service)
            .upload("fake-policy", "", "custom-name.jpg", body, MediaType.IMAGE_JPEG);

        StepVerifier.create(
                service.uploadFromUrl(url, "fake-policy", "", "custom-name.jpg")
            )
            .expectNext(uploadedAttachment)
            .verifyComplete();
    }
}


