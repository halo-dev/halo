package run.halo.app.notification;

import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * {@link NotificationTemplateRender} is used to render the notification template.
 *
 * @author guqing
 * @since 2.10.0
 */
public interface NotificationTemplateRender {

    Mono<String> render(String template, Map<String, Object> context);
}
