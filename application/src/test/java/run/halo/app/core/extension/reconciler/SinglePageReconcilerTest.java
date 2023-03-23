package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.content.TestPost.snapshotV1;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.SinglePageService;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.metrics.CounterService;

/**
 * Tests for {@link SinglePageReconciler}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class SinglePageReconcilerTest {
    @Mock
    private ExtensionClient client;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private CounterService counterService;

    @Mock
    private SinglePageService singlePageService;

    @Mock
    private ExternalUrlSupplier externalUrlSupplier;

    @InjectMocks
    private SinglePageReconciler singlePageReconciler;

    @Test
    void reconcile() {
        String name = "page-A";
        SinglePage page = pageV1();
        page.getSpec().setHeadSnapshot("page-A-head-snapshot");
        when(client.fetch(eq(SinglePage.class), eq(name)))
            .thenReturn(Optional.of(page));
        when(singlePageService.getContent(eq(page.getSpec().getHeadSnapshot()),
            eq(page.getSpec().getBaseSnapshot())))
            .thenReturn(Mono.just(ContentWrapper.builder()
                .snapshotName(page.getSpec().getHeadSnapshot())
                .raw("hello world")
                .content("<p>hello world</p>")
                .rawType("markdown")
                .build())
            );

        Snapshot snapshotV1 = snapshotV1();
        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV1.getSpec().setContributors(Set.of("guqing"));
        snapshotV2.getSpec().setContributors(Set.of("guqing", "zhangsan"));
        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(List.of(snapshotV1, snapshotV2));
        when(externalUrlSupplier.get()).thenReturn(URI.create(""));

        ArgumentCaptor<SinglePage> captor = ArgumentCaptor.forClass(SinglePage.class);
        singlePageReconciler.reconcile(new Reconciler.Request(name));

        verify(client, times(3)).update(captor.capture());

        SinglePage value = captor.getValue();
        assertThat(value.getStatus().getExcerpt()).isEqualTo("hello world");
        assertThat(value.getStatus().getContributors()).isEqualTo(List.of("guqing", "zhangsan"));
    }

    @Test
    void createPermalink() {
        SinglePage page = pageV1();
        page.getSpec().setSlug("page-slug");

        when(externalUrlSupplier.get()).thenReturn(URI.create(""));

        String permalink = singlePageReconciler.createPermalink(page);
        assertThat(permalink).isEqualTo("/page-slug");

        when(externalUrlSupplier.get()).thenReturn(URI.create("http://example.com"));
        permalink = singlePageReconciler.createPermalink(page);
        assertThat(permalink).isEqualTo("http://example.com/page-slug");

        page.getSpec().setSlug("中文 slug");
        permalink = singlePageReconciler.createPermalink(page);
        assertThat(permalink).isEqualTo("http://example.com/%E4%B8%AD%E6%96%87%20slug");
    }

    @Nested
    class LastModifyTimeTest {
        @Test
        void reconcileLastModifyTimeWhenPageIsPublished() {
            String name = "page-A";
            when(externalUrlSupplier.get()).thenReturn(URI.create(""));

            SinglePage page = pageV1();
            page.getSpec().setPublish(true);
            page.getSpec().setHeadSnapshot("page-A-head-snapshot");
            page.getSpec().setReleaseSnapshot("page-fake-released-snapshot");
            when(client.fetch(eq(SinglePage.class), eq(name)))
                .thenReturn(Optional.of(page));
            when(singlePageService.getContent(eq(page.getSpec().getHeadSnapshot()),
                eq(page.getSpec().getBaseSnapshot())))
                .thenReturn(Mono.just(ContentWrapper.builder()
                    .snapshotName(page.getSpec().getHeadSnapshot())
                    .raw("hello world")
                    .content("<p>hello world</p>")
                    .rawType("markdown")
                    .build())
                );
            Instant lastModifyTime = Instant.now();
            Snapshot snapshotV2 = TestPost.snapshotV2();
            snapshotV2.getSpec().setLastModifyTime(lastModifyTime);
            when(client.fetch(eq(Snapshot.class), eq(page.getSpec().getReleaseSnapshot())))
                .thenReturn(Optional.of(snapshotV2));

            when(client.list(eq(Snapshot.class), any(), any()))
                .thenReturn(List.of());

            ArgumentCaptor<SinglePage> captor = ArgumentCaptor.forClass(SinglePage.class);
            singlePageReconciler.reconcile(new Reconciler.Request(name));

            verify(client, times(4)).update(captor.capture());
            SinglePage value = captor.getValue();
            assertThat(value.getStatus().getLastModifyTime()).isEqualTo(lastModifyTime);
        }

        @Test
        void reconcileLastModifyTimeWhenPageIsNotPublished() {
            String name = "page-A";
            when(externalUrlSupplier.get()).thenReturn(URI.create(""));

            SinglePage page = pageV1();
            page.getSpec().setPublish(false);
            when(client.fetch(eq(SinglePage.class), eq(name)))
                .thenReturn(Optional.of(page));
            when(singlePageService.getContent(eq(page.getSpec().getHeadSnapshot()),
                eq(page.getSpec().getBaseSnapshot())))
                .thenReturn(Mono.just(ContentWrapper.builder()
                    .snapshotName(page.getSpec().getHeadSnapshot())
                    .raw("hello world")
                    .content("<p>hello world</p>")
                    .rawType("markdown")
                    .build())
                );

            when(client.list(eq(Snapshot.class), any(), any()))
                .thenReturn(List.of());

            ArgumentCaptor<SinglePage> captor = ArgumentCaptor.forClass(SinglePage.class);
            singlePageReconciler.reconcile(new Reconciler.Request(name));

            verify(client, times(3)).update(captor.capture());
            SinglePage value = captor.getValue();
            assertThat(value.getStatus().getLastModifyTime()).isNull();
        }
    }

    public static SinglePage pageV1() {
        SinglePage page = new SinglePage();
        page.setKind(Post.KIND);

        page.setApiVersion("content.halo.run/v1alpha1");
        Metadata metadata = new Metadata();
        metadata.setName("page-A");
        page.setMetadata(metadata);

        SinglePage.SinglePageSpec spec = new SinglePage.SinglePageSpec();
        page.setSpec(spec);

        spec.setTitle("page-A");
        spec.setSlug("page-slug");
        spec.setBaseSnapshot(snapshotV1().getMetadata().getName());
        spec.setHeadSnapshot("base-snapshot");
        spec.setReleaseSnapshot(null);

        return page;
    }
}