package run.halo.app.core.attachment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;

/**
 * Tests for {@link PolicyConfigChangeDetector}.
 *
 * @author guqing
 * @since 2.20.0
 */
@ExtendWith(MockitoExtension.class)
class PolicyConfigChangeDetectorTest {

    @Mock
    private PolicyConfigChangeDetector.AttachmentUpdateTrigger updateTrigger;

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
        when(client.listAllNames(same(Attachment.class), any(ListOptions.class), any(Sort.class)))
            .thenReturn(List.of("fake-attachment"));

        spyDetector.reconcile(new Reconciler.Request("fake-config"));

        verify(updateTrigger).addAll(List.of("fake-attachment"));
    }
}
