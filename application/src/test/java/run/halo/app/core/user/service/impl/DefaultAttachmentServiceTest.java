package run.halo.app.core.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.attachment.Policy.PolicySpec;
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

    DefaultAttachmentService attachmentService;

    @BeforeEach
    void setUp() {
        attachmentService =
            new DefaultAttachmentService(client, extensionGetter, dataBufferFetcher);
    }

    @Test
    void shouldUsePublicFetchersForPublicRemoteImport() throws Exception {
        var policy = new Policy();
        var policySpec = new PolicySpec();
        policySpec.setConfigMapName("fake-configmap");
        policy.setSpec(policySpec);
        policy.setMetadata(new Metadata());
        policy.getMetadata().setName("fake-policy");

        var configMap = new ConfigMap();
        configMap.setData(Map.of());

        var attachment = new Attachment();
        attachment.setMetadata(new Metadata());
        attachment.getMetadata().setName("fake-attachment");

        var handler = mock(AttachmentHandler.class);
        var dataBuffer = mock(DataBuffer.class);

        when(client.get(Policy.class, "fake-policy")).thenReturn(Mono.just(policy));
        when(client.get(ConfigMap.class, "fake-configmap")).thenReturn(Mono.just(configMap));
        when(dataBufferFetcher.headPublic(any())).thenReturn(Mono.just(new HttpHeaders()));
        when(dataBufferFetcher.fetchPublic(any())).thenReturn(Flux.just(dataBuffer));
        when(extensionGetter.getExtensions(AttachmentHandler.class)).thenReturn(Flux.just(handler));
        when(handler.upload(any())).thenReturn(Mono.just(attachment));
        when(client.create(attachment)).thenReturn(Mono.just(attachment));

        attachmentService.uploadFromPublicUrl(new URL("https://93.184.216.34/file.png"),
                "fake-policy", null, "file.png")
            .contextWrite(withAuthentication("fake-user"))
            .as(StepVerifier::create)
            .expectNext(attachment)
            .verifyComplete();

        verify(dataBufferFetcher).headPublic(any());
        verify(dataBufferFetcher).fetchPublic(any());
        verify(dataBufferFetcher, never()).head(any());
        verify(dataBufferFetcher, never()).fetch(any());
    }

    @Test
    void shouldWrapValidationErrorsForPublicRemoteImport() throws Exception {
        when(dataBufferFetcher.headPublic(any()))
            .thenReturn(Mono.error(new IllegalArgumentException("blocked")));

        attachmentService.uploadFromPublicUrl(new URL("http://127.0.0.1/file.png"),
                "fake-policy", null, "file.png")
            .contextWrite(withAuthentication("fake-user"))
            .as(StepVerifier::create)
            .consumeErrorWith(error -> {
                assertInstanceOf(ServerWebInputException.class, error);
                assertTrue(error.getMessage().contains(
                    "Failed to transfer the attachment from the external URL."));
            })
            .verify();

        verify(dataBufferFetcher).headPublic(any());
        verify(dataBufferFetcher, never()).fetchPublic(any());
    }

    private static reactor.util.context.Context withAuthentication(String username) {
        Authentication authentication = new TestingAuthenticationToken(username, "fake-password");
        return ReactiveSecurityContextHolder.withAuthentication(authentication);
    }
}
