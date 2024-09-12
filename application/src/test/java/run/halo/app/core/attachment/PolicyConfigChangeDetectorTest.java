package run.halo.app.core.attachment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;

/**
 * Tests for {@link PolicyConfigChangeDetector}.
 *
 * @author guqing
 * @since 2.20.0
 */
@ExtendWith(MockitoExtension.class)
class PolicyConfigChangeDetectorTest {

    @Mock
    private ReactiveExtensionPaginatedOperator paginatedOperator;

    @Mock
    private ReactiveExtensionClient reactiveClient;

    @Mock
    private ExtensionClient client;

    @InjectMocks
    private PolicyConfigChangeDetector policyConfigChangeDetector;

    @Test
    void reconcileTest() {
        final var spyDetector = spy(policyConfigChangeDetector);

        var configMap = new ConfigMap();
        configMap.setMetadata(new Metadata());
        configMap.getMetadata().setLabels(Map.of(Policy.POLICY_OWNER_LABEL, "fake-policy"));

        when(client.fetch(eq(ConfigMap.class), eq("fake-config")))
            .thenReturn(Optional.of(configMap));

        var attachment = new Attachment();
        attachment.setMetadata(new Metadata());
        attachment.getMetadata().setName("fake-attachment");
        attachment.setSpec(new Attachment.AttachmentSpec());
        when(paginatedOperator.list(eq(Attachment.class), any()))
            .thenReturn(Flux.just(attachment));
        when(reactiveClient.update(any())).thenReturn(Mono.empty());

        spyDetector.reconcile(new Reconciler.Request("fake-config"));

        verify(spyDetector).updateAnnotation(eq(attachment));
        verify(reactiveClient).update(assertArg(a -> assertThat(a.getMetadata().getAnnotations())
            .containsKey(PolicyConfigChangeDetector.POLICY_UPDATED_AT)));
    }
}
