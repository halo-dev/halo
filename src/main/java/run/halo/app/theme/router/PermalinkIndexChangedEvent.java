package run.halo.app.theme.router;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.util.Assert;

/**
 * Permalink index changed event.
 *
 * @author guqing
 * @since 2.0.0
 */
@Getter
public class PermalinkIndexChangedEvent extends ApplicationEvent {
    private final String oldPermalink;
    private final String permalink;

    private final GvkName gvkName;

    public PermalinkIndexChangedEvent(Object source,
        GvkName gvkName, String oldPermalink, String permalink) {
        super(source);
        Assert.notNull(gvkName, "The gvkName must not be null.");
        this.oldPermalink = oldPermalink;
        this.permalink = permalink;
        this.gvkName = gvkName;
    }
}
