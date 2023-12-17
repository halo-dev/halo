package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.theme.finders.vo.TagVo;

/**
 * Tests for {@link TagFinderImpl}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class TagFinderImplTest {

    @Mock
    private ReactiveExtensionClient client;

    private TagFinderImpl tagFinder;

    @BeforeEach
    void setUp() {
        tagFinder = new TagFinderImpl(client);
    }

    @Test
    void getByName() throws JSONException {
        when(client.fetch(eq(Tag.class), eq("t1")))
            .thenReturn(Mono.just(tag(1)));
        TagVo tagVo = tagFinder.getByName("t1").block();
        tagVo.getMetadata().setCreationTimestamp(null);
        JSONAssert.assertEquals("""
                {
                     "metadata": {
                         "name": "t1",
                         "annotations": {
                             "K1": "V1"
                         }
                     },
                     "spec": {
                         "displayName": "displayName-1",
                         "slug": "slug-1",
                         "color": "color-1",
                         "cover": "cover-1"
                     },
                     "status": {
                         "permalink": "permalink-1",
                         "postCount": 2,
                         "visiblePostCount": 1
                     },
                     "postCount": 1
                }
                """,
            JsonUtils.objectToJson(tagVo),
            true);
    }

    @Test
    void listAll() {
        when(client.list(eq(Tag.class), eq(null), any()))
            .thenReturn(Flux.fromIterable(
                    tags().stream().sorted(TagFinderImpl.DEFAULT_COMPARATOR.reversed()).toList()
                )
            );
        List<TagVo> tags = tagFinder.listAll().collectList().block();
        assertThat(tags).hasSize(3);
        assertThat(tags.stream()
            .map(tag -> tag.getMetadata().getName())
            .collect(Collectors.toList()))
            .isEqualTo(List.of("t3", "t2", "t1"));
    }

    List<Tag> tags() {
        Tag tag1 = tag(1);

        Tag tag2 = tag(2);
        tag2.getMetadata().setCreationTimestamp(Instant.now().plusSeconds(1));

        Tag tag3 = tag(3);
        tag3.getMetadata().setCreationTimestamp(Instant.now().plusSeconds(2));
        // sorted: 3, 2, 1
        return List.of(tag2, tag1, tag3);
    }

    Tag tag(int i) {
        final Tag tag = new Tag();
        Metadata metadata = new Metadata();
        metadata.setName("t" + i);
        metadata.setAnnotations(Map.of("K1", "V1"));
        metadata.setCreationTimestamp(Instant.now());
        tag.setMetadata(metadata);

        Tag.TagSpec tagSpec = new Tag.TagSpec();
        tagSpec.setDisplayName("displayName-" + i);
        tagSpec.setSlug("slug-" + i);
        tagSpec.setColor("color-" + i);
        tagSpec.setCover("cover-" + i);
        tag.setSpec(tagSpec);

        Tag.TagStatus tagStatus = new Tag.TagStatus();
        tagStatus.setPermalink("permalink-" + i);
        tagStatus.setPostCount(2);
        tagStatus.setVisiblePostCount(1);
        tag.setStatus(tagStatus);
        return tag;
    }
}