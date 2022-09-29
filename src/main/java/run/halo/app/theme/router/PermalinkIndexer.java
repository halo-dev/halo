package run.halo.app.theme.router;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import run.halo.app.content.permalinks.ExtensionLocator;
import run.halo.app.extension.GroupVersionKind;

/**
 * <p>Permalink indexer for lookup extension's name and slug by permalink.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class PermalinkIndexer {
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Map<GvkName, String> gvkNamePermalinkLookup = new HashMap<>();
    private final Map<String, ExtensionLocator> permalinkLocatorLookup = new HashMap<>();

    record GvkName(GroupVersionKind gvk, String name) {
    }

    /**
     * Register extension and permalink mapping.
     *
     * @param locator extension locator to hold the gvk and name and slug
     * @param permalink extension permalink for template route
     */
    public void register(ExtensionLocator locator, String permalink) {
        readWriteLock.writeLock().lock();
        try {
            GvkName gvkName = new GvkName(locator.gvk(), locator.name());
            gvkNamePermalinkLookup.put(gvkName, permalink);
            permalinkLocatorLookup.put(permalink, locator);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Remove extension and permalink mapping.
     *
     * @param locator extension info
     */
    public void remove(ExtensionLocator locator) {
        readWriteLock.writeLock().lock();
        try {
            String permalink =
                gvkNamePermalinkLookup.remove(new GvkName(locator.gvk(), locator.name()));
            if (permalink != null) {
                permalinkLocatorLookup.remove(permalink);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Gets permalink by {@link GroupVersionKind}.
     *
     * @param gvk group version kind
     * @return permalinks
     */
    @NonNull
    public List<String> getPermalinks(GroupVersionKind gvk) {
        readWriteLock.readLock().lock();
        try {
            return gvkNamePermalinkLookup.entrySet()
                .stream()
                .filter(entry -> entry.getKey().gvk.equals(gvk))
                .map(Map.Entry::getValue)
                .toList();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Lookup extension info by permalink.
     *
     * @param permalink extension permalink for theme template route
     * @return extension locator
     */
    @Nullable
    public ExtensionLocator lookup(String permalink) {
        readWriteLock.readLock().lock();
        try {
            return permalinkLocatorLookup.get(permalink);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Lookup extension permalink by {@link GroupVersionKind} and {@code name}.
     *
     * @param gvk group version kind
     * @param name extension name
     * @return {@code true} if contains, otherwise {@code false}
     */
    public boolean containsName(GroupVersionKind gvk, String name) {
        readWriteLock.readLock().lock();
        try {
            return gvkNamePermalinkLookup.containsKey(new GvkName(gvk, name));
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Lookup extension permalink by {@link GroupVersionKind} and {@code slug}.
     *
     * @param gvk group version kind
     * @param slug extension slug
     * @return {@code true} if contains, otherwise {@code false}
     */
    public boolean containsSlug(GroupVersionKind gvk, String slug) {
        readWriteLock.readLock().lock();
        try {
            return permalinkLocatorLookup.values()
                .stream()
                .anyMatch(locator -> locator.gvk().equals(gvk)
                    && locator.slug().equals(slug));
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Lookup extension name by resource slug.
     *
     * @param gvk extension's {@link GroupVersionKind}
     * @param slug extension resource slug
     * @return extension resource name specified by resource slug
     */
    public String getNameBySlug(GroupVersionKind gvk, String slug) {
        readWriteLock.readLock().lock();
        try {
            return permalinkLocatorLookup.values()
                .stream()
                .filter(locator -> locator.gvk().equals(gvk)
                    && locator.slug().equals(slug))
                .findFirst()
                .map(ExtensionLocator::name)
                .orElseThrow();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Get extension name by permalink.
     *
     * @param gvk is GroupVersionKind of extension
     * @param permalink is encoded permalink
     * @return extension name or null
     */
    @Nullable
    public String getNameByPermalink(GroupVersionKind gvk, String permalink) {
        readWriteLock.readLock().lock();
        try {
            var locator = permalinkLocatorLookup.get(permalink);
            return locator == null ? null : locator.name();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Only for test.
     *
     * @return permalinkLookup map size
     */
    protected long gvkNamePermalinkMapSize() {
        return gvkNamePermalinkLookup.size();
    }

    /**
     * Only for test.
     *
     * @return permalinkLocatorMap map size
     */
    protected long permalinkLocatorMapSize() {
        return permalinkLocatorLookup.size();
    }

    /**
     * Add a record to the {@link PermalinkIndexer}.
     * If permalink already exists, it will not be added to indexer
     *
     * @param addCommand a command to add a record to {@link PermalinkIndexer}
     */
    @EventListener(PermalinkIndexAddCommand.class)
    public void onPermalinkAdd(PermalinkIndexAddCommand addCommand) {
        if (checkPermalinkExists(addCommand.getLocator(), addCommand.getPermalink())) {
            // TODO send an extension event to log this error
            log.error("Permalink [{}] already exists, you can try to change the slug [{}].",
                addCommand.getPermalink(), addCommand.getLocator());
            return;
        }
        register(addCommand.getLocator(), addCommand.getPermalink());
    }

    @EventListener(PermalinkIndexDeleteCommand.class)
    public void onPermalinkDelete(PermalinkIndexDeleteCommand deleteCommand) {
        remove(deleteCommand.getLocator());
    }

    /**
     * Update a {@link PermalinkIndexer} record by {@link ExtensionLocator} and permalink.
     * If permalink already exists, it will not be updated
     *
     * @param updateCommand a command to update an indexer record
     */
    @EventListener(PermalinkIndexUpdateCommand.class)
    public void onPermalinkUpdate(PermalinkIndexUpdateCommand updateCommand) {
        if (checkPermalinkExists(updateCommand.getLocator(), updateCommand.getPermalink())) {
            // TODO send an extension event to log this error
            log.error("Permalink [{}] already exists, you can try to change the slug [{}].",
                updateCommand.getPermalink(), updateCommand.getLocator());
            return;
        }
        remove(updateCommand.getLocator());
        register(updateCommand.getLocator(), updateCommand.getPermalink());
    }

    private boolean checkPermalinkExists(ExtensionLocator locator, String permalink) {
        ExtensionLocator lookup = lookup(permalink);
        return lookup != null && !lookup.equals(locator);
    }
}
