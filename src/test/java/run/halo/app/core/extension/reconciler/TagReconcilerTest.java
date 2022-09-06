package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.content.permalinks.TagPermalinkPolicy;
import run.halo.app.core.extension.Tag;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;

/**
 * Tests for {@link TagReconciler}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class TagReconcilerTest {
    @Mock
    private ExtensionClient client;

    @Mock
    private TagPermalinkPolicy tagPermalinkPolicy;

    @InjectMocks
    private TagReconciler tagReconciler;

    @Test
    void reconcile() {
        Tag tag = tag();
        when(client.fetch(eq(Tag.class), eq("fake-tag")))
            .thenReturn(Optional.of(tag));
        when(tagPermalinkPolicy.permalink(any()))
            .thenAnswer(arg -> "/tags/" + tag.getSpec().getSlug());
        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);

        tagReconciler.reconcile(new TagReconciler.Request("fake-tag"));

        verify(client, times(1)).update(captor.capture());
        verify(tagPermalinkPolicy, times(1)).onPermalinkAdd(any());
        verify(tagPermalinkPolicy, times(1)).onPermalinkDelete(any());
        Tag capture = captor.getValue();
        assertThat(capture.getStatus().getPermalink()).isEqualTo("/tags/fake-slug");

        // change slug
        tag.getSpec().setSlug("new-slug");
        tagReconciler.reconcile(new TagReconciler.Request("fake-tag"));
        verify(client, times(2)).update(captor.capture());
        verify(tagPermalinkPolicy, times(2)).onPermalinkAdd(any());
        verify(tagPermalinkPolicy, times(2)).onPermalinkDelete(any());
        assertThat(capture.getStatus().getPermalink()).isEqualTo("/tags/new-slug");
    }

    @Test
    void reconcileDelete() {
        Tag tag = tag();
        tag.getMetadata().setDeletionTimestamp(Instant.now());
        when(client.fetch(eq(Tag.class), eq("fake-tag")))
            .thenReturn(Optional.of(tag));
        when(tagPermalinkPolicy.permalink(any()))
            .thenAnswer(arg -> "/tags/" + tag.getSpec().getSlug());
        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);

        tagReconciler.reconcile(new TagReconciler.Request("fake-tag"));
        verify(client, times(1)).update(captor.capture());
        verify(tagPermalinkPolicy, times(0)).onPermalinkAdd(any());
        verify(tagPermalinkPolicy, times(1)).onPermalinkDelete(any());
        verify(tagPermalinkPolicy, times(1)).permalink(any());
    }

    Tag tag() {
        Tag tag = new Tag();
        tag.setMetadata(new Metadata());
        tag.getMetadata().setName("fake-tag");

        tag.setSpec(new Tag.TagSpec());
        tag.getSpec().setSlug("fake-slug");

        tag.setStatus(new Tag.TagStatus());
        return tag;
    }
}