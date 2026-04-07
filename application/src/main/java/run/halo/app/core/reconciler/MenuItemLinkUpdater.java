package run.halo.app.core.reconciler;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.MenuItem.MenuItemStatus;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Ref;
import run.halo.app.extension.Watcher;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.infra.InitializationPhase;

/**
 * Watches changes to Post, Category, Tag, and SinglePage to keep MenuItem link statuses
 * up-to-date in an event-driven manner instead of polling.
 *
 * @author johnniang
 * @since 2.22.0
 */
@Slf4j
@Component
public class MenuItemLinkUpdater implements SmartLifecycle {

    private static final Set<GroupVersionKind> WATCHED_GVKS =
        Set.of(Post.GVK, Category.GVK, Tag.GVK, SinglePage.GVK);

    private final ExtensionClient client;

    private volatile boolean running = false;

    private Watcher watcher;

    public MenuItemLinkUpdater(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public void start() {
        watcher = new LinkedResourceWatcher();
        client.watch(watcher);
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        if (watcher != null) {
            watcher.dispose();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return InitializationPhase.CONTROLLERS.getPhase();
    }

    void reconcileMenuItems(Extension extension) {
        var name = extension.getMetadata().getName();
        var gvk = extension.groupVersionKind();
        var options = ListOptions.builder()
            .fieldQuery(Queries.equal("spec.targetRef.name", name))
            .build();
        var menuItems = client.listAll(MenuItem.class, options, Sort.unsorted());
        for (var menuItem : menuItems) {
            var targetRef = menuItem.getSpec().getTargetRef();
            if (targetRef == null || !Ref.groupKindEquals(targetRef, gvk)) {
                continue;
            }
            updateMenuItemStatus(menuItem, extension);
        }
    }

    private void updateMenuItemStatus(MenuItem menuItem, Extension extension) {
        String href = null;
        String displayName = null;

        if (extension instanceof Post post) {
            if (post.getStatus() != null
                && StringUtils.hasText(post.getStatus().getPermalink())) {
                href = post.getStatus().getPermalink();
                displayName = post.getSpec().getTitle();
            }
        } else if (extension instanceof Category category) {
            if (category.getStatus() != null
                && StringUtils.hasText(category.getStatus().getPermalink())) {
                href = category.getStatus().getPermalink();
                displayName = category.getSpec().getDisplayName();
            }
        } else if (extension instanceof Tag tag) {
            if (tag.getStatus() != null
                && StringUtils.hasText(tag.getStatus().getPermalink())) {
                href = tag.getStatus().getPermalink();
                displayName = tag.getSpec().getDisplayName();
            }
        } else if (extension instanceof SinglePage page) {
            if (page.getStatus() != null
                && StringUtils.hasText(page.getStatus().getPermalink())) {
                href = page.getStatus().getPermalink();
                displayName = page.getSpec().getTitle();
            }
        }

        if (href == null || displayName == null) {
            return;
        }

        var currentStatus = menuItem.getStatus();
        if (currentStatus != null
            && href.equals(currentStatus.getHref())
            && displayName.equals(currentStatus.getDisplayName())) {
            return;
        }

        var newStatus = new MenuItemStatus();
        newStatus.setHref(href);
        newStatus.setDisplayName(displayName);
        menuItem.setStatus(newStatus);
        client.update(menuItem);
    }

    class LinkedResourceWatcher implements Watcher {

        private volatile boolean disposed = false;

        private Runnable disposeHook;

        @Override
        public void onAdd(Extension extension) {
            if (disposed) {
                return;
            }
            if (!WATCHED_GVKS.contains(extension.groupVersionKind())) {
                return;
            }
            reconcileMenuItems(extension);
        }

        @Override
        public void onUpdate(Extension oldExtension, Extension newExtension) {
            if (disposed) {
                return;
            }
            if (!WATCHED_GVKS.contains(newExtension.groupVersionKind())) {
                return;
            }
            reconcileMenuItems(newExtension);
        }

        @Override
        public void registerDisposeHook(Runnable dispose) {
            this.disposeHook = dispose;
        }

        @Override
        public void dispose() {
            disposed = true;
            if (disposeHook != null) {
                disposeHook.run();
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}
