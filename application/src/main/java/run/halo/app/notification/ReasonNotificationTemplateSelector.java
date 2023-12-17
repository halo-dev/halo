package run.halo.app.notification;

import java.util.Locale;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.NotificationTemplate;
import run.halo.app.core.extension.notification.ReasonType;
import run.halo.app.extension.Metadata;

/**
 * Reason notification template selector to select notification template by reason type and locale.
 *
 * @author guqing
 * @see NotificationTemplate
 * @see ReasonType
 * @since 2.10.0
 */
public interface ReasonNotificationTemplateSelector {

    /**
     * Select notification template by reason type and locale.
     * <p>Locale order is important: as we will let values from more specific to less specific (e.g.
     * a value for gl_ES will have more precedence than a value for gl).</p>
     * <p>If specific locale found and has multiple templates, we will order them by
     * {@link Metadata#getCreationTimestamp()} and return the latest one.</p>
     *
     * @param reasonType reason type
     * @param locale locale
     * @return notification template if found, or empty
     */
    Mono<NotificationTemplate> select(String reasonType, Locale locale);
}
