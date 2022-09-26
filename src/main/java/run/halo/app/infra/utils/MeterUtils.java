package run.halo.app.infra.utils;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.apache.commons.lang3.StringUtils;

/**
 * Meter utils.
 *
 * @author guqing
 * @since 2.0.0
 */
public class MeterUtils {

    public static final Tag METRICS_COMMON_TAG = Tag.of("metrics.halo.run", "true");

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
            return String.join(".", plural, name);
        }
        return String.join(".", group, plural, name);
    }

    /**
     * Build a {@link Counter} for halo extension.
     *
     * @param registry meter registry
     * @param name counter name,build by {@link #nameOf(String, String, String)}
     * @return counter find by name from registry if exists, otherwise create a new one.
     */
    public static Counter counter(MeterRegistry registry, String name) {
        Counter counter = registry.find(name)
            .counter();
        if (counter == null) {
            return registry.counter(name, Tags.of(METRICS_COMMON_TAG));
        }
        return counter;
    }
}
