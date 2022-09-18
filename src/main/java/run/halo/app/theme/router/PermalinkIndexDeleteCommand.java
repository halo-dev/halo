package run.halo.app.theme.router;

import org.springframework.context.ApplicationEvent;
import run.halo.app.content.permalinks.ExtensionLocator;

/**
 * <p>Send a command to delete a piece of data from {@link PermalinkIndexer}.</p>
 *
 * @author guqing
 * @see PermalinkIndexer
 * @since 2.0.0
 */
public class PermalinkIndexDeleteCommand extends ApplicationEvent {
    private final ExtensionLocator locator;

    public PermalinkIndexDeleteCommand(Object source, ExtensionLocator locator) {
        super(source);
        this.locator = locator;
    }

    public ExtensionLocator getLocator() {
        return locator;
    }
}
