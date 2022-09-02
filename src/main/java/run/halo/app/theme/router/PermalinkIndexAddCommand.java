package run.halo.app.theme.router;

import org.springframework.context.ApplicationEvent;
import run.halo.app.content.permalinks.ExtensionLocator;

/**
 * <p>Send a command to add a piece of data from {@link PermalinkIndexer}.</p>
 *
 * @author guqing
 * @see PermalinkIndexer
 * @since 2.0.0
 */
public class PermalinkIndexAddCommand extends ApplicationEvent {
    private final ExtensionLocator locator;
    private final String permalink;

    public PermalinkIndexAddCommand(Object source, ExtensionLocator locator, String permalink) {
        super(source);
        this.locator = locator;
        this.permalink = permalink;
    }

    public ExtensionLocator getLocator() {
        return locator;
    }

    public String getPermalink() {
        return permalink;
    }
}
