package run.halo.app.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link DefaultIndexer}.
 *
 * @author guqing
 * @since 2.0.0
 */
class DefaultIndexerTest {
    @Test
    public void testTagsIndexer() throws JSONException {
        // Create a new Indexer that indexes Post objects by tags.
        DefaultIndexer<Post> indexer = new DefaultIndexer<>();
        String tagsIndexName = "tags";
        indexer.addIndexFunc(tagsIndexName, post -> {
            List<String> tags = post.getSpec().getTags();
            return tags == null ? Set.of() : Set.copyOf(tags);
        });

        // Create some Post objects.
        Post post1 = new Post();
        post1.setMetadata(new Metadata());
        post1.getMetadata().setName("post-1");
        post1.setSpec(new Post.PostSpec());
        post1.getSpec().setTags(List.of("t1", "t2"));

        Post post2 = new Post();
        post2.setMetadata(new Metadata());
        post2.getMetadata().setName("post-2");
        post2.setSpec(new Post.PostSpec());
        post2.getSpec().setTags(List.of("t2", "t3"));

        Post post3 = new Post();
        post3.setMetadata(new Metadata());
        post3.getMetadata().setName("post-3");
        post3.setSpec(new Post.PostSpec());
        post3.getSpec().setTags(List.of("t3", "t4"));

        // Add the Post objects to the Indexer.
        indexer.add(tagsIndexName, post1);
        indexer.add(tagsIndexName, post2);
        indexer.add(tagsIndexName, post3);

        // Verify that the Indexer has the correct indices.
        JSONAssert.assertEquals("""
                {
                    "t4": [
                        "post-3"
                    ],
                    "t3": [
                        "post-2",
                        "post-3"
                    ],
                    "t2": [
                        "post-1",
                        "post-2"
                    ],
                    "t1": [
                        "post-1"
                    ]
                }
                """,
            JsonUtils.objectToJson(indexer.getIndices("tags")),
            true);

        // Remove post2 from the Indexer.
        indexer.delete(tagsIndexName, post2);

        // Verify that the Indexer has the correct indices.
        JSONAssert.assertEquals("""
                {
                    "t1": [
                        "post-1"
                    ],
                    "t2": [
                        "post-1"
                    ],
                    "t3": [
                        "post-3"
                    ],
                    "t4": [
                        "post-3"
                    ]
                }
                """,
            JsonUtils.objectToJson(indexer.getIndices("tags")),
            true);

        // Update post3 in the Indexer.
        post3.getSpec().setTags(List.of("t4", "t5"));
        indexer.update(tagsIndexName, post3);

        // Verify that the Indexer has the correct indices.
        JSONAssert.assertEquals("""
                {
                    "t1": [
                        "post-1"
                    ],
                    "t2": [
                        "post-1"
                    ],
                    "t4": [
                        "post-3"
                    ],
                    "t5": [
                        "post-3"
                    ]
                }
                """,
            JsonUtils.objectToJson(indexer.getIndices("tags")),
            true);
    }

    @Test
    public void testLabelIndexer() throws JSONException {
        // Create a new Indexer.
        DefaultIndexer<Post> indexer = new DefaultIndexer<>();

        // Define the IndexFunc for labels.
        DefaultIndexer.IndexFunc<Post> labelIndexFunc = labelIndexFunc();

        // Add the label IndexFunc to the Indexer.
        String labelsIndexName = "labels";
        indexer.addIndexFunc(labelsIndexName, labelIndexFunc);

        // Create some posts with labels.
        Post post1 = new Post();
        post1.setMetadata(new Metadata());
        post1.getMetadata().setName("post-1");
        post1.getMetadata().setLabels(Map.of("app", "myapp", "env", "prod"));

        Post post2 = new Post();
        post2.setMetadata(new Metadata());
        post2.getMetadata().setName("post-2");
        post2.getMetadata().setLabels(Map.of("app", "myapp", "env", "test"));

        Post post3 = new Post();
        post3.setMetadata(new Metadata());
        post3.getMetadata().setName("post-3");
        post3.getMetadata().setLabels(Map.of("app", "otherapp", "env", "prod"));

        // Add the posts to the Indexer.
        indexer.add(labelsIndexName, post1);
        indexer.add(labelsIndexName, post2);
        indexer.add(labelsIndexName, post3);

        // Verify that the Indexer has the correct indices.
        assertEquals(
            Map.of(
                "app=myapp", Set.of("post-1", "post-2"),
                "app=otherapp", Set.of("post-3"),
                "env=test", Set.of("post-2"),
                "env=prod", Set.of("post-1", "post-3")
            ),
            indexer.getIndices("labels"));

        // Delete post2 from the Indexer.
        indexer.delete(labelsIndexName, post2);

        // Verify that the Indexer has the correct indices.
        JSONAssert.assertEquals("""
                {
                    "app=myapp": [
                        "post-1"
                    ],
                    "env=prod": [
                        "post-1",
                        "post-3"
                    ],
                    "app=otherapp": [
                        "post-3"
                    ]
                }
                """,
            JsonUtils.objectToJson(indexer.getIndices("labels")),
            true);

        // Update post2 in the Indexer.
        post2.getMetadata().setLabels(Map.of("l1", "v1", "l2", "v2"));
        indexer.update(labelsIndexName, post2);

        // Verify that the Indexer has the correct indices.
        JSONAssert.assertEquals("""
                {
                    "app=myapp": [
                        "post-1"
                    ],
                    "env=prod": [
                        "post-1",
                        "post-3"
                    ],
                    "app=otherapp": [
                        "post-3"
                    ],
                    "l1=v1": [
                        "post-2"
                    ],
                    "l2=v2": [
                        "post-2"
                    ]
                }
                """,
            JsonUtils.objectToJson(indexer.getIndices("labels")),
            true);

        // Update post1 in the Indexer.
        post1.getMetadata().setLabels(Map.of("l2", "v2", "l3", "v3"));
        indexer.update(labelsIndexName, post1);

        // Verify that the Indexer has the correct indices.
        JSONAssert.assertEquals("""
                {
                    "env=prod": [
                        "post-3"
                    ],
                    "app=otherapp": [
                        "post-3"
                    ],
                    "l1=v1": [
                        "post-2"
                    ],
                    "l2=v2": [
                        "post-1",
                        "post-2"
                    ],
                    "l3=v3": [
                        "post-1"
                    ]
                }
                """,
            JsonUtils.objectToJson(indexer.getIndices("labels")),
            true);
    }

    @Test
    void multiIndexName() {
        // Create a new Indexer.
        DefaultIndexer<Post> indexer = new DefaultIndexer<>();

        // Define the IndexFunc for labels.
        String labelsIndexName = "labels";
        DefaultIndexer.IndexFunc<Post> labelIndexFunc = labelIndexFunc();
        indexer.addIndexFunc(labelsIndexName, labelIndexFunc);

        String tagsIndexName = "tags";
        indexer.addIndexFunc(tagsIndexName, post -> {
            List<String> tags = post.getSpec().getTags();
            return tags == null ? Set.of() : Set.copyOf(tags);
        });

        Post post1 = new Post();
        post1.setMetadata(new Metadata());
        post1.getMetadata().setName("post-1");
        post1.getMetadata().setLabels(Map.of("app", "myapp", "env", "prod"));
        post1.setSpec(new Post.PostSpec());
        post1.getSpec().setTags(List.of("t1", "t2"));

        Post post2 = new Post();
        post2.setMetadata(new Metadata());
        post2.getMetadata().setName("post-2");
        post2.getMetadata().setLabels(Map.of("app", "myapp", "env", "test"));
        post2.setSpec(new Post.PostSpec());
        post2.getSpec().setTags(List.of("t2", "t3"));

        indexer.add(labelsIndexName, post1);
        indexer.add(tagsIndexName, post1);

        indexer.add(labelsIndexName, post2);
        indexer.add(tagsIndexName, post2);

        assertThat(indexer.getByIndex(labelsIndexName, "app=myapp"))
            .containsExactlyInAnyOrder("post-1", "post-2");
        assertThat(indexer.getByIndex(tagsIndexName, "t1"))
            .containsExactlyInAnyOrder("post-1");

        assertThat(indexer.getByIndex(labelsIndexName, "env=test"))
            .containsExactlyInAnyOrder("post-2");
        assertThat(indexer.getByIndex(tagsIndexName, "t2"))
            .containsExactlyInAnyOrder("post-1", "post-2");

        post2.getSpec().setTags(List.of("t1", "t4"));
        indexer.update(tagsIndexName, post2);

        assertThat(indexer.getByIndex(tagsIndexName, "t1"))
            .containsExactlyInAnyOrder("post-1", "post-2");
        assertThat(indexer.getByIndex(tagsIndexName, "t2"))
            .containsExactlyInAnyOrder("post-1");
        assertThat(indexer.getByIndex(tagsIndexName, "t4"))
            .containsExactlyInAnyOrder("post-2");
    }

    private static DefaultIndexer.IndexFunc<Post> labelIndexFunc() {
        return post -> {
            Map<String, String> labels = post.getMetadata().getLabels();
            Set<String> indexKeys = new HashSet<>();
            if (labels != null) {
                for (Map.Entry<String, String> entry : labels.entrySet()) {
                    indexKeys.add(entry.getKey() + "=" + entry.getValue());
                }
            }
            return indexKeys;
        };
    }

    @Test
    void getByIndex() {
        DefaultIndexer<Post> indexer = new DefaultIndexer<>();
        String tagsIndexName = "tags";
        indexer.addIndexFunc(tagsIndexName, post -> {
            List<String> tags = post.getSpec().getTags();
            return tags == null ? Set.of() : Set.copyOf(tags);
        });

        // Create some Post objects.
        Post post1 = new Post();
        post1.setMetadata(new Metadata());
        post1.getMetadata().setName("post-1");
        post1.setSpec(new Post.PostSpec());
        post1.getSpec().setTags(List.of("t1", "t2"));

        Post post2 = new Post();
        post2.setMetadata(new Metadata());
        post2.getMetadata().setName("post-2");
        post2.setSpec(new Post.PostSpec());
        post2.getSpec().setTags(List.of("t2", "t3"));

        indexer.add(tagsIndexName, post1);
        indexer.add(tagsIndexName, post2);

        assertThat(indexer.getByIndex(tagsIndexName, "t1"))
            .containsExactlyInAnyOrder("post-1");
        assertThat(indexer.getByIndex(tagsIndexName, "t2"))
            .containsExactlyInAnyOrder("post-1", "post-2");
        assertThat(indexer.getByIndex(tagsIndexName, "t3"))
            .containsExactlyInAnyOrder("post-2");
    }

    @Test
    void addButNotIndexFunc() {
        // Create some Post objects.
        Post post1 = new Post();
        post1.setMetadata(new Metadata());
        post1.getMetadata().setName("post-1");
        post1.setSpec(new Post.PostSpec());
        post1.getSpec().setTags(List.of("t1", "t2"));

        // Create a new Indexer that indexes Post objects by tags.
        final DefaultIndexer<Post> indexer = new DefaultIndexer<>();
        assertThrows(IllegalArgumentException.class, () -> {
            indexer.add("fake-index-name", post1);
        }, "Index function not found for index name 'fake-index-name'");
    }
}
