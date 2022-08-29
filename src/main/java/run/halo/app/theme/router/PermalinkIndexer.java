package run.halo.app.theme.router;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import run.halo.app.content.permalinks.ExtensionLocator;
import run.halo.app.extension.GroupVersionKind;

/**
 * <p>Permalink indexer for lookup extension's name and slug by permalink.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PermalinkIndexer {
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final MultiValueMap<GroupVersionKind, String> permalinkLookup =
        new LinkedMultiValueMap<>();
    private final Map<String, ExtensionLocator> permalinkLocatorMap = new HashMap<>();

    /**
     * Register extension and permalink mapping.
     *
     * @param locator extension locator to hold the gvk and name and slug
     * @param permalink extension permalink for template route
     */
    public void register(ExtensionLocator locator, String permalink) {
        readWriteLock.writeLock().lock();
        try {
            permalinkLookup.add(locator.gvk(), permalink);
            permalinkLocatorMap.put(permalink, locator);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Remove extension and permalink mapping.
     *
     * @param locator extension locator
     * @param permalink extension permalink for theme template route
     */
    public void remove(ExtensionLocator locator, String permalink) {
        readWriteLock.writeLock().lock();
        try {
            List<String> permalinks = permalinkLookup.get(locator.gvk());
            if (permalinks != null) {
                permalinks.remove(permalink);
            }
            permalinkLocatorMap.remove(permalink);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Lookup extension locator by permalink.
     *
     * @param permalink extension permalink for theme template route
     * @return extension locator
     */
    public ExtensionLocator lookup(String permalink) {
        readWriteLock.readLock().lock();
        try {
            return permalinkLocatorMap.get(permalink);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Gets permalinks by extension's {@link GroupVersionKind}.
     *
     * @param gvk extension's {@link GroupVersionKind}
     * @return permalinks for extension's {@link GroupVersionKind}
     */
    public List<String> getPermalinks(GroupVersionKind gvk) {
        readWriteLock.readLock().lock();
        try {
            return permalinkLookup.get(gvk);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Lookup extension resource names by {@link GroupVersionKind}.
     *
     * @param gvk extension's {@link GroupVersionKind}
     * @return extension resource names
     */
    public List<String> getNames(GroupVersionKind gvk) {
        readWriteLock.readLock().lock();
        try {
            return permalinkLocatorMap.values()
                .stream()
                .filter(locator -> locator.gvk().equals(gvk))
                .map(ExtensionLocator::name)
                .toList();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Lookup extension resource slugs by {@link GroupVersionKind}.
     *
     * @param gvk extension's {@link GroupVersionKind}
     * @return extension resource slugs
     */
    public List<String> getSlugs(GroupVersionKind gvk) {
        readWriteLock.readLock().lock();
        try {
            return permalinkLocatorMap.values()
                .stream()
                .filter(locator -> locator.gvk().equals(gvk))
                .map(ExtensionLocator::slug)
                .toList();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Lookup extension slug by resource name.
     *
     * @param gvk extension's {@link GroupVersionKind}
     * @param name extension resource name
     * @return extension slug specified by resource name
     */
    public String getSlugByName(GroupVersionKind gvk, String name) {
        readWriteLock.readLock().lock();
        try {
            return permalinkLocatorMap.values()
                .stream()
                .filter(locator -> locator.gvk().equals(gvk))
                .filter(locator -> locator.name().equals(name))
                .findFirst()
                .map(ExtensionLocator::slug)
                .orElseThrow();
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
            return permalinkLocatorMap.values()
                .stream()
                .filter(locator -> locator.gvk().equals(gvk))
                .filter(locator -> locator.slug().equals(slug))
                .findFirst()
                .map(ExtensionLocator::name)
                .orElseThrow();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @EventListener(PermalinkIndexAddCommand.class)
    public void onPermalinkAdd(PermalinkIndexAddCommand addCommand) {
        register(addCommand.getLocator(), addCommand.getPermalink());
    }

    @EventListener(PermalinkIndexDeleteCommand.class)
    public void onPermalinkDelete(PermalinkIndexDeleteCommand deleteCommand) {
        register(deleteCommand.getLocator(), deleteCommand.getPermalink());
    }

    @EventListener(PermalinkIndexUpdateCommand.class)
    public void onPermalinkUpdate(PermalinkIndexUpdateCommand updateCommand) {
        remove(updateCommand.getLocator(), updateCommand.getPermalink());
        register(updateCommand.getLocator(), updateCommand.getPermalink());
    }
}
