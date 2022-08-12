package run.halo.app.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import run.halo.app.content.impl.PostServiceImpl;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Metadata;

/**
 * @author guqing
 * @since 2.0.0
 */
@WithMockUser(username = "guqing")
@ExtendWith(SpringExtension.class)
class PostServiceTest {
    @Mock
    private ExtensionClient client;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostServiceImpl(client);
    }

    @Test
    void draftPost() {
        when(client.fetch(eq(Post.class), eq("post-A")))
            .thenReturn(Optional.of(postV1()));
        when(client.fetch(eq(Snapshot.class), eq("snapshot-A")))
            .thenReturn(Optional.of(snapshotV1()));

        PostRequest postRequest = new PostRequest(postV1(), contentRequest("B", "<p>B</p>"));

        ArgumentCaptor<AbstractExtension> captor = ArgumentCaptor.forClass(AbstractExtension.class);
        StepVerifier.create(postService.draftPost(postRequest))
            .expectNext(postV1())
            .expectComplete()
            .verify();

        verify(client, times(2)).create(captor.capture());
        List<AbstractExtension> allValues = captor.getAllValues();
        assertThat(allValues.size()).isEqualTo(2);
        assertThat(allValues.get(0)).isInstanceOf(Post.class);
        assertThat(allValues.get(1)).isInstanceOf(Snapshot.class);
        Post expectPost = postV1();
        expectPost.getSpec().setOwner("guqing");
        assertThat(allValues.get(0)).isEqualTo(expectPost);

        Snapshot expectSnapshot = snapshotV1();
        expectSnapshot.getMetadata().setName(allValues.get(1).getMetadata().getName());
        expectSnapshot.setSubjectRef(Post.KIND, expectPost.getMetadata().getName());
        expectSnapshot.getSpec().setRawPatch("B");
        expectSnapshot.getSpec().setContentPatch("<p>B</p>");
        assertThat(allValues.get(1)).isEqualTo(expectSnapshot);
    }

    @Test
    void updatePost() {
    }

    @Test
    void publishPost() {
    }

    ContentRequest contentRequest(String raw, String content) {
        return new ContentRequest(raw, content, "MARKDOWN");
    }

    Post postV1() {
        Post post = new Post();
        post.setKind(Post.KIND);
        post.setApiVersion(getApiVersion(Post.class));
        Metadata metadata = new Metadata();
        metadata.setName("post-A");
        post.setMetadata(metadata);

        Post.PostSpec postSpec = new Post.PostSpec();
        post.setSpec(postSpec);

        postSpec.setTitle("post-A");
        postSpec.setVersion(1);
        postSpec.setBaseSnapshot(snapshotV1().getMetadata().getName());
        postSpec.setHeadSnapshot("base-snapshot");
        postSpec.setReleaseSnapshot(null);

        return post;
    }

    Snapshot snapshotV1() {
        Snapshot snapshot = new Snapshot();
        snapshot.setKind(Snapshot.KIND);
        snapshot.setApiVersion(getApiVersion(Snapshot.class));
        Metadata metadata = new Metadata();
        metadata.setName("snapshot-A");
        snapshot.setMetadata(metadata);
        Snapshot.SnapShotSpec spec = new Snapshot.SnapShotSpec();
        snapshot.setSpec(spec);

        spec.setDisplayVersion("v1");
        spec.setVersion(1);
        snapshot.addContributor("guqing");
        spec.setRawType("MARKDOWN");
        spec.setRawPatch("A");
        spec.setContentPatch("<p>A</p>");

        return snapshot;
    }

    Snapshot snapshotV2() {
        Snapshot snapshot = new Snapshot();
        snapshot.setKind(Snapshot.KIND);
        snapshot.setApiVersion(getApiVersion(Snapshot.class));
        Metadata metadata = new Metadata();
        metadata.setName("snapshot-B");
        snapshot.setMetadata(metadata);
        Snapshot.SnapShotSpec spec = new Snapshot.SnapShotSpec();
        snapshot.setSpec(spec);

        spec.setDisplayVersion("v2");
        spec.setVersion(2);
        snapshot.addContributor("guqing");
        spec.setRawType("MARKDOWN");
        spec.setRawPatch(PatchUtils.diffToJsonPatch("A", "B"));
        spec.setContentPatch(PatchUtils.diffToJsonPatch("<p>A</p>", "<p>B</p>"));

        return snapshot;
    }

    public String getApiVersion(Class<? extends AbstractExtension> extension) {
        GVK annotation = extension.getAnnotation(GVK.class);
        return annotation.group() + "/" + annotation.version();
    }
}