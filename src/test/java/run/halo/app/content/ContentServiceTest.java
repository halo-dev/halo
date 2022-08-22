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
        Snapshot.SubjectRef subjectRef = Snapshot.SubjectRef.of(Post.KIND, "test-post");
        snapshotV1.getSpec().setSubjectRef(subjectRef);

        ContentRequest contentRequest =
            new ContentRequest(subjectRef, null,
                snapshotV1.getSpec().getRawPatch(),
                snapshotV1.getSpec().getContentPatch(),
                snapshotV1.getSpec().getRawType());

        pilingBaseSnapshot(snapshotV1);

        ContentWrapper contentWrapper =
            new ContentWrapper("snapshot-A", contentRequest.raw(),
                contentRequest.content(), snapshotV1.getSpec().getRawType());

        ArgumentCaptor<Snapshot> captor = ArgumentCaptor.forClass(Snapshot.class);
        when(client.create(any())).thenReturn(Mono.just(snapshotV1));

        StepVerifier.create(contentService.draftContent(contentRequest))
            .expectNext(contentWrapper)
            .expectComplete()
            .verify();

        verify(client, times(1)).create(captor.capture());
        Snapshot snapshot = captor.getValue();

        snapshotV1.getMetadata().setName(snapshot.getMetadata().getName());
        snapshotV1.getSpec().setSubjectRef(subjectRef);
        assertThat(snapshot).isEqualTo(snapshotV1);
    }

    @Test
    void updateContent() {
        String headSnapshot = "snapshot-A";
        Snapshot snapshotV1 = snapshotV1();
        Snapshot.SubjectRef subjectRef = Snapshot.SubjectRef.of(Post.KIND, "test-post");

        Snapshot updated = snapshotV1();
        updated.getSpec().setRawPatch("hello");
        updated.getSpec().setContentPatch("<p>hello</p>");
        updated.getSpec().setSubjectRef(subjectRef);
        ContentRequest contentRequest =
            new ContentRequest(subjectRef, headSnapshot,
                snapshotV1.getSpec().getRawPatch(),
                snapshotV1.getSpec().getContentPatch(),
                snapshotV1.getSpec().getRawType());

        pilingBaseSnapshot(snapshotV1);

        when(client.fetch(eq(Snapshot.class), eq(contentRequest.headSnapshotName())))
            .thenReturn(Mono.just(updated));

        ContentWrapper contentWrapper =
            new ContentWrapper(headSnapshot, contentRequest.raw(),
                contentRequest.content(), snapshotV1.getSpec().getRawType());

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

        Snapshot snapshotV2 = snapshotV2();
        snapshotV2.getSpec().setPublishTime(null);


        // v1(released),v2
        snapshotV1.getSpec().setPublishTime(Instant.now());
        pilingBaseSnapshot(snapshotV2, snapshotV1);

        when(client.fetch(eq(Snapshot.class), eq(snapshotV2.getMetadata().getName())))
            .thenReturn(Mono.just(snapshotV2));
        when(client.fetch(eq(Snapshot.class), eq(snapshotV1.getMetadata().getName())))
            .thenReturn(Mono.just(snapshotV1));

        Snapshot.SubjectRef subjectRef = Snapshot.SubjectRef.of(Post.KIND, "test-post");

        ContentRequest contentRequest =
            new ContentRequest(subjectRef, headSnapshot, "C",
                "<p>C</p>", snapshotV1.getSpec().getRawType());

        when(client.create(any())).thenReturn(Mono.just(snapshotV3()));

        StepVerifier.create(contentService.latestSnapshotVersion(subjectRef))
            .expectNext(snapshotV2)
            .expectComplete()
            .verify();

        StepVerifier.create(contentService.updateContent(contentRequest))
            .consumeNextWith(created -> {
                assertThat(created.raw()).isEqualTo("C");
                assertThat(created.content()).isEqualTo("<p>C</p>");
            })
            .expectComplete()
            .verify();
    }

    @Test
    void updateContentWhenHeadPoints2Published() {
        Snapshot.SubjectRef subjectRef = Snapshot.SubjectRef.of(Post.KIND, "test-post");

        // v1(released),v2
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getSpec().setPublishTime(Instant.now());
        snapshotV1.getSpec().setSubjectRef(subjectRef);

        Snapshot snapshotV2 = snapshotV2();
        snapshotV2.getSpec().setSubjectRef(subjectRef);
        snapshotV2.getSpec().setPublishTime(null);

        final String headSnapshot = snapshotV2.getMetadata().getName();


        pilingBaseSnapshot(snapshotV2, snapshotV1);

        when(client.fetch(eq(Snapshot.class), eq(snapshotV2.getMetadata().getName())))
            .thenReturn(Mono.just(snapshotV2));
        when(client.fetch(eq(Snapshot.class), eq(snapshotV1.getMetadata().getName())))
            .thenReturn(Mono.just(snapshotV1));

        ContentRequest contentRequest =
            new ContentRequest(subjectRef, headSnapshot, "C",
                "<p>C</p>", snapshotV1.getSpec().getRawType());

        when(client.update(any())).thenReturn(Mono.just(snapshotV2()));

        StepVerifier.create(contentService.latestSnapshotVersion(subjectRef))
            .expectNext(snapshotV2)
            .expectComplete()
            .verify();

        StepVerifier.create(contentService.updateContent(contentRequest))
            .consumeNextWith(updated -> {
                assertThat(updated.raw()).isEqualTo("C");
                assertThat(updated.content()).isEqualTo("<p>C</p>");
            })
            .expectComplete()
            .verify();

        verify(client, times(1)).update(any());
    }

    @Test
    void publishContent() {
        Snapshot.SubjectRef subjectRef = Snapshot.SubjectRef.of(Post.KIND, "test-post");

        // v1(released),v2
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getSpec().setPublishTime(null);
        snapshotV1.getSpec().setSubjectRef(subjectRef);

        final String headSnapshot = snapshotV1.getMetadata().getName();

        pilingBaseSnapshot(snapshotV1);

        when(client.fetch(eq(Snapshot.class), eq(snapshotV1.getMetadata().getName())))
            .thenReturn(Mono.just(snapshotV1));

        when(client.update(any())).thenReturn(Mono.just(snapshotV2()));

        StepVerifier.create(contentService.publish(headSnapshot, subjectRef))
            .expectNext()
            .consumeNextWith(p -> {
                System.out.println(JsonUtils.objectToJson(p));
            })
            .expectComplete()
            .verify();
        // has benn published,do nothing
        verify(client, times(1)).update(any());
    }

    @Test
    void publishContentWhenHasPublishedThenDoNothing() {
        Snapshot.SubjectRef subjectRef = Snapshot.SubjectRef.of(Post.KIND, "test-post");

        // v1(released),v2
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getSpec().setPublishTime(Instant.now());
        snapshotV1.getSpec().setSubjectRef(subjectRef);

        final String headSnapshot = snapshotV1.getMetadata().getName();

        pilingBaseSnapshot(snapshotV1);

        when(client.fetch(eq(Snapshot.class), eq(snapshotV1.getMetadata().getName())))
            .thenReturn(Mono.just(snapshotV1));

        when(client.update(any())).thenReturn(Mono.just(snapshotV2()));

        StepVerifier.create(contentService.publish(headSnapshot, subjectRef))
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
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getSpec().setPublishTime(Instant.now());
        snapshotV1.setSubjectRef(Post.KIND, postName);

        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV2.setSubjectRef(Post.KIND, postName);

        Snapshot snapshotV3 = TestPost.snapshotV3();
        snapshotV3.setSubjectRef(Post.KIND, postName);

        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(Flux.just(snapshotV2, snapshotV1, snapshotV3));

        Snapshot.SubjectRef subjectRef = Snapshot.SubjectRef.of(Post.KIND, postName);
        StepVerifier.create(contentService.getBaseSnapshot(subjectRef))
            .expectNext(snapshotV1)
            .expectComplete()
            .verify();
    }

    @Test
    void latestSnapshotVersion() {
        String postName = "post-1";
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getSpec().setPublishTime(Instant.now());
        snapshotV1.setSubjectRef(Post.KIND, postName);
        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV2.setSubjectRef(Post.KIND, postName);

        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(Flux.just(snapshotV1, snapshotV2));

        Snapshot.SubjectRef subjectRef = Snapshot.SubjectRef.of(Post.KIND, postName);
        StepVerifier.create(contentService.latestSnapshotVersion(subjectRef))
            .expectNext(snapshotV2)
            .expectComplete()
            .verify();

        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(Flux.just(snapshotV1, snapshotV2, snapshotV3()));
        StepVerifier.create(contentService.latestSnapshotVersion(subjectRef))
            .expectNext(snapshotV3())
            .expectComplete()
            .verify();
    }

    @Test
    void latestPublishedSnapshotThenV1() {
        String postName = "post-1";
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.setSubjectRef(Post.KIND, postName);
        snapshotV1.getSpec().setPublishTime(Instant.now());

        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV2.setSubjectRef(Post.KIND, postName);
        snapshotV2.getSpec().setPublishTime(null);

        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(Flux.just(snapshotV1, snapshotV2));

        Snapshot.SubjectRef subjectRef = Snapshot.SubjectRef.of(Post.KIND, postName);
        StepVerifier.create(contentService.latestPublishedSnapshot(subjectRef))
            .expectNext(snapshotV1)
            .expectComplete()
            .verify();
    }

    @Test
    void latestPublishedSnapshotThenV2() {
        String postName = "post-1";
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.setSubjectRef(Post.KIND, postName);
        snapshotV1.getSpec().setPublishTime(Instant.now());

        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV2.setSubjectRef(Post.KIND, postName);
        snapshotV2.getSpec().setPublishTime(Instant.now());

        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(Flux.just(snapshotV2, snapshotV1));

        Snapshot.SubjectRef subjectRef = Snapshot.SubjectRef.of(Post.KIND, postName);

        StepVerifier.create(contentService.latestPublishedSnapshot(subjectRef))
            .expectNext(snapshotV2)
            .expectComplete()
            .verify();
    }

}