package run.halo.app.event.menu;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that is published after a MenuItem has been reconciled.
 *
 * @author ryanwang
 * @since 2.24.0
 */
public class MenuItemReconciledEvent extends ApplicationEvent {

    @Getter
    private final String menuItemName;

    public MenuItemReconciledEvent(Object source, String menuItemName) {
        super(source);
        this.menuItemName = menuItemName;
    }
}
