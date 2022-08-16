package run.halo.app.content;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static run.halo.app.content.TestPost.snapshotV1;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import run.halo.app.content.impl.ContentServiceImpl;
import run.halo.app.content.impl.PostServiceImpl;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.ExtensionClient;

/**
 * Tests for {@link ContentService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ExtensionClient client;

    private ContentService contentService;

    @BeforeEach
    void setUp() {
        contentService = new ContentServiceImpl(client);
    }

    @Test
    void latestSnapshotVersion() {
        String postName = "post-1";
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.setSubjectRef(Post.KIND, postName);
        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV2.setSubjectRef(Post.KIND, postName);

        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(Stream.of(snapshotV1, snapshotV2)
                .sorted(PostServiceImpl.LATEST_SNAPSHOT_COMPARATOR).toList());

        Snapshot.SubjectRef subjectRef = Snapshot.SubjectRef.of(Post.KIND, postName);
        StepVerifier.create(contentService.latestSnapshotVersion(subjectRef))
            .expectNext(snapshotV2)
            .expectComplete()
            .verify();
    }

}