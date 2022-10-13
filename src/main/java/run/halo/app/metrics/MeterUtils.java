package run.halo.app.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Meter utils.
 *
 * @author guqing
 * @since 2.0.0
 */
public class MeterUtils {

    public static final Tag METRICS_COMMON_TAG = Tag.of("metrics.halo.run", "true");
    public static final String SCENE = "scene";
    public static final String VISIT_SCENE = "visit";
    public static final String UPVOTE_SCENE = "upvote";
    public static final String DOWNVOTE_SCENE = "downvote";
    public static final String TOTAL_COMMENT_SCENE = "total_comment";
    public static final String APPROVED_COMMENT_SCENE = "approved_comment";

    /**
     * Build a counter name.
     *
     * @param group extension group
     * @param plural extension plural
     * @param name extension name
     * @return counter name
     */
    public static String nameOf(String group, String plural, String name) {
        if (StringUtils.isBlank(group)) {
            return String.join("/", plural, name);
        }
        return String.join(".", plural, group) + "/" + name;
    }

    public static <T extends AbstractExtension> String nameOf(Class<T> clazz, String name) {
        GVK annotation = clazz.getAnnotation(GVK.class);
        return nameOf(annotation.group(), annotation.plural(), name);
    }

    public static Counter visitCounter(MeterRegistry registry, String name) {
        return counter(registry, name, Tag.of(SCENE, VISIT_SCENE));
    }

    public static Counter upvoteCounter(MeterRegistry registry, String name) {
        return counter(registry, name, Tag.of(SCENE, UPVOTE_SCENE));
    }

    public static Counter downvoteCounter(MeterRegistry registry, String name) {
        return counter(registry, name, Tag.of(SCENE, DOWNVOTE_SCENE));
    }

    public static Counter totalCommentCounter(MeterRegistry registry, String name) {
        return counter(registry, name, Tag.of(SCENE, TOTAL_COMMENT_SCENE));
    }

    public static Counter approvedCommentCounter(MeterRegistry registry, String name) {
        return counter(registry, name, Tag.of(SCENE, APPROVED_COMMENT_SCENE));
    }

    public static boolean isVisitCounter(Counter counter) {
        String sceneValue = counter.getId().getTag(SCENE);
        if (StringUtils.isBlank(sceneValue)) {
            return false;
        }
        return VISIT_SCENE.equals(sceneValue);
    }

    public static boolean isUpvoteCounter(Counter counter) {
        String sceneValue = counter.getId().getTag(SCENE);
        if (StringUtils.isBlank(sceneValue)) {
            return false;
        }
        return UPVOTE_SCENE.equals(sceneValue);
    }

    public static boolean isDownvoteCounter(Counter counter) {
        String sceneValue = counter.getId().getTag(SCENE);
        if (StringUtils.isBlank(sceneValue)) {
            return false;
        }
        return DOWNVOTE_SCENE.equals(sceneValue);
    }

    public static boolean isTotalCommentCounter(Counter counter) {
        String sceneValue = counter.getId().getTag(SCENE);
        if (StringUtils.isBlank(sceneValue)) {
            return false;
        }
        return TOTAL_COMMENT_SCENE.equals(sceneValue);
    }

    public static boolean isApprovedCommentCounter(Counter counter) {
        String sceneValue = counter.getId().getTag(SCENE);
        if (StringUtils.isBlank(sceneValue)) {
            return false;
        }
        return APPROVED_COMMENT_SCENE.equals(sceneValue);
    }

    /**
     * Build a {@link Counter} for halo extension.
     *
     * @param registry meter registry
     * @param name counter name,build by {@link #nameOf(String, String, String)}
     * @return counter find by name from registry if exists, otherwise create a new one.
     */
    private static Counter counter(MeterRegistry registry, String name, Tag... tags) {
        Tags withTags = Tags.of(METRICS_COMMON_TAG).and(tags);
        Counter counter = registry.find(name)
            .tags(withTags)
            .counter();
        if (counter == null) {
            return registry.counter(name, withTags);
        }
        return counter;
    }
}
