package run.halo.app.content.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;

/**
 * Tests for {@link ContentServiceImpl}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ContentServiceImplTest {

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private ContentServiceImpl contentService;

    @Test
    void getBaseSnapshot() {
        Snapshot snapshotV1 = TestPost.snapshotV1();
        ExtensionUtil.nullSafeAnnotations(snapshotV1)
            .put(Snapshot.KEEP_RAW_ANNO, "true");
        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(Flux.just(TestPost.snapshotV2(), snapshotV1, TestPost.snapshotV3()));
        contentService.getBaseSnapshot(Ref.of("fake-post"))
            .as(StepVerifier::create)
            .consumeNextWith(
                baseSnapshot -> assertThat(baseSnapshot.getMetadata().getName())
                    .isEqualTo(snapshotV1.getMetadata().getName()))
            .verifyComplete();
    }

    @Test
    void latestSnapshotVersion() {
        Snapshot snapshotV1 = TestPost.snapshotV1();
        snapshotV1.getMetadata().setCreationTimestamp(Instant.now());

        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV2.getMetadata().setCreationTimestamp(Instant.now().plusSeconds(2));

        Snapshot snapshotV3 = TestPost.snapshotV3();
        snapshotV3.getMetadata().setCreationTimestamp(Instant.now().plusSeconds(3));

        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(Flux.just(snapshotV2, snapshotV1, snapshotV3));

        contentService.latestSnapshotVersion(Ref.of("fake-post"))
            .as(StepVerifier::create)
            .consumeNextWith(s -> {
                assertThat(s.getMetadata().getName()).isEqualTo(snapshotV3.getMetadata().getName());
            })
            .verifyComplete();
    }
}