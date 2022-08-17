package run.halo.app.content;

import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Metadata;

/**
 * @author guqing
 * @since 2.0.0
 */
public class TestPost {
    public static Post postV1() {
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

    public static Snapshot snapshotV1() {
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

    public static Snapshot snapshotV2() {
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

    public static Snapshot snapshotV3() {
        Snapshot snapshotV3 = snapshotV2();
        snapshotV3.getMetadata().setName("snapshot-C");
        Snapshot.SnapShotSpec spec = snapshotV3.getSpec();
        spec.setDisplayVersion("v3");
        spec.setVersion(3);
        snapshotV3.addContributor("guqing");
        spec.setRawType("MARKDOWN");
        spec.setRawPatch(PatchUtils.diffToJsonPatch("B", "C"));
        spec.setContentPatch(PatchUtils.diffToJsonPatch("<p>B</p>", "<p>C</p>"));

        return snapshotV3;
    }

    public static String getApiVersion(Class<? extends AbstractExtension> extension) {
        GVK annotation = extension.getAnnotation(GVK.class);
        return annotation.group() + "/" + annotation.version();
    }
}
