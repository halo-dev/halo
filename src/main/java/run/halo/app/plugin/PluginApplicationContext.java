package run.halo.app.plugin;

import java.util.List;
import java.util.concurrent.locks.StampedLock;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import run.halo.app.extension.GroupVersionKind;

/**
 * The generic IOC container for plugins.
 * The plugin-classes loaded through the same plugin-classloader will be put into the same
 * {@link PluginApplicationContext} for bean creation.
 *
 * @author guqing
 * @since 2.0.0
 */
public class PluginApplicationContext extends GenericApplicationContext {

    private final GvkExtensionMapping gvkExtensionMapping = new GvkExtensionMapping();

    private String pluginId;

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * Gets the gvk-extension mapping.
     * It is thread safe
     *
     * @param gvk the group-kind-version
     * @param extensionName extension resources name
     */
    public void addExtensionMapping(GroupVersionKind gvk, String extensionName) {
        gvkExtensionMapping.addExtensionMapping(gvk, extensionName);
    }

    /**
     * Gets the extension names by gvk.
     * It is thread safe
     *
     * @param gvk the group-kind-version
     * @return a immutable list of extension names
     */
    public List<String> getExtensionNames(GroupVersionKind gvk) {
        return List.copyOf(gvkExtensionMapping.getExtensionNames(gvk));
    }

    public MultiValueMap<GroupVersionKind, String> extensionNamesMapping() {
        return gvkExtensionMapping.extensionNamesMapping();
    }

    static class GvkExtensionMapping {
        private final StampedLock sl = new StampedLock();
        private final MultiValueMap<GroupVersionKind, String> extensionNamesMapping =
            new LinkedMultiValueMap<>();

        public void addAllExtensionMapping(GroupVersionKind gvk, List<String> extensionNames) {
            long stamp = sl.writeLock();
            try {
                extensionNamesMapping.addAll(gvk, extensionNames);
            } finally {
                sl.unlockWrite(stamp);
            }
        }

        public void addExtensionMapping(GroupVersionKind gvk, String extensionName) {
            long stamp = sl.writeLock();
            try {
                extensionNamesMapping.add(gvk, extensionName);
            } finally {
                sl.unlockWrite(stamp);
            }
        }

        public List<String> getExtensionNames(GroupVersionKind gvk) {
            Assert.notNull(gvk, "The gvk must not be null");
            long stamp = sl.tryOptimisticRead();
            List<String> values = extensionNamesMapping.get(gvk);
            if (!sl.validate(stamp)) {
                // Check if another write lock occurs after the optimistic read lock
                // If so, escalate lock to a pessimistic lock
                stamp = sl.readLock();
                try {
                    return extensionNamesMapping.get(gvk);
                } finally {
                    sl.unlockRead(stamp);
                }
            }
            return values;
        }

        public MultiValueMap<GroupVersionKind, String> extensionNamesMapping() {
            return new LinkedMultiValueMap<>(extensionNamesMapping);
        }

        public void clear() {
            extensionNamesMapping.clear();
        }
    }

    @Override
    protected void onClose() {
        // For subclasses: do nothing by default.
        super.onClose();
        gvkExtensionMapping.clear();
    }
}
