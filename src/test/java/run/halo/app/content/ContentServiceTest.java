package run.halo.app.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.content.TestPost.snapshotV1;
import static run.halo.app.content.TestPost.snapshotV2;
import static run.halo.app.content.TestPost.snapshotV3;

import java.time.Instant;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.content.impl.ContentServiceImpl;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link ContentService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@WithMockUser(username = "guqing")
@ExtendWith(SpringExtension.class)
class ContentServiceTest {

    @Mock
    private ReactiveExtensionClient client;

    private ContentService contentService;

    @BeforeEach
    void setUp() {
        contentService = new ContentServiceImpl(client);
    }

    @Test
    void draftContent() {
        Snapshot snapshotV1 = snapshotV1();
        Ref ref = postRef("test-post");
        snapshotV1.getSpec().setSubjectRef(ref);

        ContentRequest contentRequest =
            new ContentRequest(ref, null,
                snapshotV1.getSpec().getRawPatch(),
                snapshotV1.getSpec().getContentPatch(),
                snapshotV1.getSpec().getRawType());

        pilingBaseSnapshot(snapshotV1);
        ContentWrapper contentWrapper = ContentWrapper.builder()
            .snapshotName("snapshot-A")
            .version(1)
            .raw(contentRequest.raw())
            .content(contentRequest.content())
            .rawType(snapshotV1.getSpec().getRawType())
            .build();

        ArgumentCaptor<Snapshot> captor = ArgumentCaptor.forClass(Snapshot.class);
        when(client.create(any())).thenReturn(Mono.just(snapshotV1));

        StepVerifier.create(contentService.draftContent(contentRequest))
            .expectNext(contentWrapper)
            .expectComplete()
            .verify();

        verify(client, times(1)).create(captor.capture());
        Snapshot snapshot = captor.getValue();

        snapshotV1.getMetadata().setName(snapshot.getMetadata().getName());
        snapshotV1.getSpec().setSubjectRef(ref);
        assertThat(snapshot).isEqualTo(snapshotV1);
    }

    @Test
    void updateContent() {
        String headSnapshot = "snapshot-A";
        Snapshot snapshotV1 = snapshotV1();
        Ref ref = postRef("test-post");

        Snapshot updated = snapshotV1();
        updated.getSpec().setRawPatch("hello");
        updated.getSpec().setContentPatch("<p>hello</p>");
        updated.getSpec().setSubjectRef(ref);
        ContentRequest contentRequest =
            new ContentRequest(ref, headSnapshot,
                snapshotV1.getSpec().getRawPatch(),
                snapshotV1.getSpec().getContentPatch(),
                snapshotV1.getSpec().getRawType());

        pilingBaseSnapshot(snapshotV1);

        when(client.fetch(eq(Snapshot.class), eq(contentRequest.headSnapshotName())))
            .thenReturn(Mono.just(updated));

        ContentWrapper contentWrapper = ContentWrapper.builder()
            .snapshotName(headSnapshot)
            .version(1)
            .raw(contentRequest.raw())
            .content(contentRequest.content())
            .rawType(snapshotV1.getSpec().getRawType())
            .build();

        ArgumentCaptor<Snapshot> captor = ArgumentCaptor.forClass(Snapshot.class);
        when(client.update(any())).thenReturn(Mono.just(updated));

        StepVerifier.create(contentService.updateContent(contentRequest))
            .expectNext(contentWrapper)
            .expectComplete()
            .verify();

        verify(client, times(1)).update(captor.capture());
        Snapshot snapshot = captor.getValue();

        assertThat(snapshot).isEqualTo(updated);
    }

    @Test
    void updateContentWhenHasDraftVersionButHeadPoints2Published() {
        final String headSnapshot = "snapshot-A";
        Snapshot snapshotV1 = snapshotV1();
        final Ref ref = postRef("test-post");

        Snapshot snapshotV2 = snapshotV2();
        snapshotV2.getSpec().setPublishTime(null);


        // v1(released),v2
        snapshotV1.getMetadata().setLabels(new HashMap<>());
        Snapshot.putPublishedLabel(snapshotV1.getMetadata().getLabels());
        snapshotV1.getSpec().setPublishTime(Instant.now());
        pilingBaseSnapshot(snapshotV2, snapshotV1);

        when(client.fetch(eq(Snapshot.class), eq(snapshotV2.getMetadata().getName())))
            .thenReturn(Mono.just(snapshotV2));
        when(client.fetch(eq(Snapshot.class), eq(snapshotV1.getMetadata().getName())))
            .thenReturn(Mono.just(snapshotV1));

        final ContentRequest contentRequest =
            new ContentRequest(ref, headSnapshot, "C",
                "<p>C</p>", snapshotV1.getSpec().getRawType());

        when(client.create(any())).thenReturn(Mono.just(snapshotV3()));

        StepVerifier.create(contentService.latestSnapshotVersion(ref))
            .expectNext(snapshotV2)
            .expectComplete()
            .verify();

        Snapshot publishedV2 = snapshotV2();
        publishedV2.getMetadata().setLabels(new HashMap<>());
        Snapshot.putPublishedLabel(publishedV2.getMetadata().getLabels());
        when(client.update(any())).thenReturn(Mono.just(publishedV2));
        StepVerifier.create(contentService.updateContent(contentRequest))
            .consumeNextWith(created -> {
                assertThat(created.getRaw()).isEqualTo("C");
                assertThat(created.getContent()).isEqualTo("<p>C</p>");
            })
            .expectComplete()
            .verify();
    }

    @Test
    void updateContentWhenHeadPoints2Published() {
        final Ref ref = postRef("test-post");
        // v1(released),v2
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getMetadata().setLabels(new HashMap<>());
        Snapshot.putPublishedLabel(snapshotV1.getMetadata().getLabels());
        snapshotV1.getSpec().setPublishTime(Instant.now());
        snapshotV1.getSpec().setSubjectRef(ref);

        Snapshot snapshotV2 = snapshotV2();
        snapshotV2.getSpec().setSubjectRef(ref);
        snapshotV2.getSpec().setPublishTime(null);

        final String headSnapshot = snapshotV2.getMetadata().getName();


        pilingBaseSnapshot(snapshotV2, snapshotV1);

        when(client.fetch(eq(Snapshot.class), eq(snapshotV2.getMetadata().getName())))
            .thenReturn(Mono.just(snapshotV2));
        when(client.fetch(eq(Snapshot.class), eq(snapshotV1.getMetadata().getName())))
            .thenReturn(Mono.just(snapshotV1));

        ContentRequest contentRequest =
            new ContentRequest(ref, headSnapshot, "C",
                "<p>C</p>", snapshotV1.getSpec().getRawType());

        when(client.update(any())).thenReturn(Mono.just(snapshotV2()));

        StepVerifier.create(contentService.latestSnapshotVersion(ref))
            .expectNext(snapshotV2)
            .expectComplete()
            .verify();

        StepVerifier.create(contentService.updateContent(contentRequest))
            .consumeNextWith(updated -> {
                assertThat(updated.getRaw()).isEqualTo("C");
                assertThat(updated.getContent()).isEqualTo("<p>C</p>");
            })
            .expectComplete()
            .verify();

        verify(client, times(1)).update(any());
    }

    @Test
    void publishContent() {
        Ref ref = postRef("test-post");
        // v1(released),v2
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getSpec().setPublishTime(null);
        snapshotV1.getSpec().setSubjectRef(ref);

        final String headSnapshot = snapshotV1.getMetadata().getName();

        pilingBaseSnapshot(snapshotV1);

        when(client.fetch(eq(Snapshot.class), eq(snapshotV1.getMetadata().getName())))
            .thenReturn(Mono.just(snapshotV1));

        when(client.update(any())).thenReturn(Mono.just(snapshotV2()));

        StepVerifier.create(contentService.publish(headSnapshot, ref))
            .expectNext()
            .consumeNextWith(p -> {
                System.out.println(JsonUtils.objectToJson(p));
            })
            .expectComplete()
            .verify();
        // has benn published,do nothing
        verify(client, times(1)).update(any());
    }

    private static Ref postRef(String name) {
        Ref ref = new Ref();
        ref.setGroup("content.halo.run");
        ref.setVersion("v1alpha1");
        ref.setKind(Post.KIND);
        ref.setName(name);
        return ref;
    }

    @Test
    void publishContentWhenHasPublishedThenDoNothing() {
        final Ref ref = postRef("test-post");

        // v1(released),v2
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getMetadata().setLabels(new HashMap<>());
        Snapshot.putPublishedLabel(snapshotV1.getMetadata().getLabels());
        snapshotV1.getSpec().setPublishTime(Instant.now());
        snapshotV1.getSpec().setSubjectRef(ref);

        final String headSnapshot = snapshotV1.getMetadata().getName();

        pilingBaseSnapshot(snapshotV1);

        when(client.fetch(eq(Snapshot.class), eq(snapshotV1.getMetadata().getName())))
            .thenReturn(Mono.just(snapshotV1));

        when(client.update(any())).thenReturn(Mono.just(snapshotV2()));

        StepVerifier.create(contentService.publish(headSnapshot, ref))
            .expectNext()
            .consumeNextWith(p -> {
                System.out.println(JsonUtils.objectToJson(p));
            })
            .expectComplete()
            .verify();
        // has benn published,do nothing
        verify(client, times(0)).update(any());
    }

    private void pilingBaseSnapshot(Snapshot... expected) {
        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(Flux.just(expected));
    }

    @Test
    void baseSnapshotVersion() {
        String postName = "post-1";
        final Ref ref = postRef(postName);
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getMetadata().setLabels(new HashMap<>());
        Snapshot.putPublishedLabel(snapshotV1.getMetadata().getLabels());
        snapshotV1.getSpec().setPublishTime(Instant.now());
        snapshotV1.getSpec().setSubjectRef(ref);

        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV2.getSpec().setSubjectRef(ref);

        Snapshot snapshotV3 = TestPost.snapshotV3();
        snapshotV3.getSpec().setSubjectRef(ref);

        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(Flux.just(snapshotV2, snapshotV1, snapshotV3));

        StepVerifier.create(contentService.getBaseSnapshot(ref))
            .expectNext(snapshotV1)
            .expectComplete()
            .verify();
    }

    @Test
    void latestSnapshotVersion() {
        String postName = "post-1";
        final Ref ref = postRef(postName);
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getMetadata().setLabels(new HashMap<>());
        Snapshot.putPublishedLabel(snapshotV1.getMetadata().getLabels());
        snapshotV1.getSpec().setPublishTime(Instant.now());
        snapshotV1.getSpec().setSubjectRef(ref);
        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV2.getSpec().setSubjectRef(ref);

        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(Flux.just(snapshotV1, snapshotV2));

        StepVerifier.create(contentService.latestSnapshotVersion(ref))
            .expectNext(snapshotV2)
            .expectComplete()
            .verify();

        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(Flux.just(snapshotV1, snapshotV2, snapshotV3()));
        StepVerifier.create(contentService.latestSnapshotVersion(ref))
            .expectNext(snapshotV3())
            .expectComplete()
            .verify();
    }

    @Test
    void latestPublishedSnapshotThenV1() {
        String postName = "post-1";
        Ref ref = postRef(postName);
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getSpec().setSubjectRef(ref);
        snapshotV1.getMetadata().setLabels(new HashMap<>());
        Snapshot.putPublishedLabel(snapshotV1.getMetadata().getLabels());
        snapshotV1.getSpec().setPublishTime(Instant.now());

        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV2.getSpec().setSubjectRef(ref);
        snapshotV2.getSpec().setPublishTime(null);

        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(Flux.just(snapshotV1, snapshotV2));

        StepVerifier.create(contentService.latestPublishedSnapshot(ref))
            .expectNext(snapshotV1)
            .expectComplete()
            .verify();
    }

    @Test
    void latestPublishedSnapshotThenV2() {
        String postName = "post-1";
        Ref ref = postRef(postName);
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getSpec().setSubjectRef(ref);
        snapshotV1.getMetadata().setLabels(new HashMap<>());
        Snapshot.putPublishedLabel(snapshotV1.getMetadata().getLabels());
        snapshotV1.getSpec().setPublishTime(Instant.now());

        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV2.getSpec().setSubjectRef(ref);
        snapshotV2.getMetadata().setLabels(new HashMap<>());
        Snapshot.putPublishedLabel(snapshotV2.getMetadata().getLabels());
        snapshotV2.getSpec().setPublishTime(Instant.now());

        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(Flux.just(snapshotV2, snapshotV1));

        StepVerifier.create(contentService.latestPublishedSnapshot(ref))
            .expectNext(snapshotV2)
            .expectComplete()
            .verify();
    }

}