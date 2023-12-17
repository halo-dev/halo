package run.halo.app.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.RequiredSearch;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import run.halo.app.core.extension.content.Post;

/**
 * Tests for {@link MeterUtils}.
 *
 * @author guqing
 * @since 2.0.0
 */
class MeterUtilsTest {

    @Test
    void nameOf() {
        String s = MeterUtils.nameOf(Post.class, "fake-post");
        assertThat(s).isEqualTo("posts.content.halo.run/fake-post");
    }

    @Test
    void testNameOf() {
        String s = MeterUtils.nameOf("content.halo.run", "posts", "fake-post");
        assertThat(s).isEqualTo("posts.content.halo.run/fake-post");
    }

    @Test
    void visitCounter() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        MeterUtils.visitCounter(meterRegistry, "posts.content.halo.run/fake-post")
            .increment();
        RequiredSearch requiredSearch = meterRegistry.get("posts.content.halo.run/fake-post");
        assertThat(requiredSearch.counter().count()).isEqualTo(1);
        Meter.Id id = requiredSearch.counter().getId();
        assertThat(id.getTag(MeterUtils.SCENE)).isEqualTo(MeterUtils.VISIT_SCENE);
        assertThat(id.getTag(MeterUtils.METRICS_COMMON_TAG.getKey()))
            .isEqualTo(MeterUtils.METRICS_COMMON_TAG.getValue());
    }

    @Test
    void upvoteCounter() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        MeterUtils.upvoteCounter(meterRegistry, "posts.content.halo.run/fake-post")
            .increment(2);
        RequiredSearch requiredSearch = meterRegistry.get("posts.content.halo.run/fake-post");
        assertThat(requiredSearch.counter().count()).isEqualTo(2);
        Meter.Id id = requiredSearch.counter().getId();
        assertThat(id.getTag(MeterUtils.SCENE)).isEqualTo(MeterUtils.UPVOTE_SCENE);
        assertThat(id.getTag(MeterUtils.METRICS_COMMON_TAG.getKey()))
            .isEqualTo(MeterUtils.METRICS_COMMON_TAG.getValue());
    }

    @Test
    void totalCommentCounter() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        MeterUtils.totalCommentCounter(meterRegistry, "content.halo.run.posts.fake-post")
            .increment(3);
        RequiredSearch requiredSearch = meterRegistry.get("content.halo.run.posts.fake-post");
        assertThat(requiredSearch.counter().count()).isEqualTo(3);
        Meter.Id id = requiredSearch.counter().getId();
        assertThat(id.getTag(MeterUtils.SCENE)).isEqualTo(MeterUtils.TOTAL_COMMENT_SCENE);
        assertThat(id.getTag(MeterUtils.METRICS_COMMON_TAG.getKey()))
            .isEqualTo(MeterUtils.METRICS_COMMON_TAG.getValue());
    }

    @Test
    void approvedCommentCounter() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        MeterUtils.approvedCommentCounter(meterRegistry, "posts.content.halo.run/fake-post")
            .increment(2);
        RequiredSearch requiredSearch = meterRegistry.get("posts.content.halo.run/fake-post");
        assertThat(requiredSearch.counter().count()).isEqualTo(2);
        Meter.Id id = requiredSearch.counter().getId();
        assertThat(id.getTag(MeterUtils.SCENE)).isEqualTo(MeterUtils.APPROVED_COMMENT_SCENE);
        assertThat(id.getTag(MeterUtils.METRICS_COMMON_TAG.getKey()))
            .isEqualTo(MeterUtils.METRICS_COMMON_TAG.getValue());
    }

    @Test
    void isVisitCounter() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        Counter visitCounter =
            MeterUtils.visitCounter(meterRegistry, "posts.content.halo.run/fake-post");
        assertThat(MeterUtils.isVisitCounter(visitCounter)).isTrue();
    }

    @Test
    void isUpvoteCounter() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        Counter upvoteCounter =
            MeterUtils.upvoteCounter(meterRegistry, "posts.content.halo.run/fake-post");
        assertThat(MeterUtils.isUpvoteCounter(upvoteCounter)).isTrue();
        assertThat(MeterUtils.isVisitCounter(upvoteCounter)).isFalse();
    }

    @Test
    void isDownvoteCounter() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        Counter downvoteCounter =
            MeterUtils.downvoteCounter(meterRegistry, "posts.content.halo.run/fake-post");
        assertThat(MeterUtils.isDownvoteCounter(downvoteCounter)).isTrue();
        assertThat(MeterUtils.isVisitCounter(downvoteCounter)).isFalse();
    }

    @Test
    void isTotalCommentCounter() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        Counter totalCommentCounter =
            MeterUtils.totalCommentCounter(meterRegistry, "posts.content.halo.run/fake-post");
        assertThat(MeterUtils.isTotalCommentCounter(totalCommentCounter)).isTrue();
        assertThat(MeterUtils.isVisitCounter(totalCommentCounter)).isFalse();
    }

    @Test
    void isApprovedCommentCounter() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        Counter approvedCommentCounter =
            MeterUtils.approvedCommentCounter(meterRegistry, "posts.content.halo.run/fake-post");
        assertThat(MeterUtils.isApprovedCommentCounter(approvedCommentCounter)).isTrue();
        assertThat(MeterUtils.isVisitCounter(approvedCommentCounter)).isFalse();
    }
}