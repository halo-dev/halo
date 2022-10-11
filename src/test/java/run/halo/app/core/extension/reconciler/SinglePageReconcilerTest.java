package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.content.TestPost.snapshotV1;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentService;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.theme.router.PermalinkIndexAddCommand;
import run.halo.app.theme.router.PermalinkIndexDeleteCommand;
import run.halo.app.theme.router.PermalinkIndexUpdateCommand;

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
    private ContentService contentService;

    @Mock
    private ApplicationContext applicationContext;

    private SinglePageReconciler singlePageReconciler;

    @BeforeEach
    void setUp() {
        singlePageReconciler = new SinglePageReconciler(client, contentService, applicationContext);
    }

    @Test
    void reconcile() {
        String name = "page-A";
        SinglePage page = pageV1();
        page.getSpec().setHeadSnapshot("page-A-head-snapshot");
        when(client.fetch(eq(SinglePage.class), eq(name)))
            .thenReturn(Optional.of(page));
        when(contentService.getContent(eq(page.getSpec().getHeadSnapshot())))
            .thenReturn(Mono.just(
                new ContentWrapper(page.getSpec().getHeadSnapshot(), "hello world",
                    "<p>hello world</p>", "markdown")));

        Snapshot snapshotV1 = snapshotV1();
        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV1.getSpec().setContributors(Set.of("guqing"));
        snapshotV2.getSpec().setContributors(Set.of("guqing", "zhangsan"));
        when(contentService.listSnapshots(any()))
            .thenReturn(Flux.just(snapshotV1, snapshotV2));

        ArgumentCaptor<SinglePage> captor = ArgumentCaptor.forClass(SinglePage.class);
        singlePageReconciler.reconcile(new Reconciler.Request(name));

        verify(client, times(3)).update(captor.capture());

        SinglePage value = captor.getValue();
        assertThat(value.getStatus().getExcerpt()).isEqualTo("hello world");
        assertThat(value.getStatus().getContributors()).isEqualTo(List.of("guqing", "zhangsan"));

        verify(applicationContext, times(0)).publishEvent(isA(PermalinkIndexAddCommand.class));
        verify(applicationContext, times(1)).publishEvent(isA(PermalinkIndexDeleteCommand.class));
        verify(applicationContext, times(0)).publishEvent(isA(PermalinkIndexUpdateCommand.class));
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
        spec.setVersion(1);
        spec.setBaseSnapshot(snapshotV1().getMetadata().getName());
        spec.setHeadSnapshot("base-snapshot");
        spec.setReleaseSnapshot(null);

        return page;
    }
}