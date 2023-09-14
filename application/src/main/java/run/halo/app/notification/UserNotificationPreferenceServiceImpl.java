package run.halo.app.notification;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

/**
 * User notification preference service implementation.
 *
 * @author guqing
 * @since 2.10.0
 */
@Component
@RequiredArgsConstructor
public class UserNotificationPreferenceServiceImpl implements UserNotificationPreferenceService {

    public static final String NOTIFICATION_PREFERENCE = "notification";

    private final ReactiveExtensionClient client;

    @Override
    public Mono<UserNotificationPreference> getByUser(String username) {
        var configName = buildUserPreferenceConfigMapName(username);
        return client.fetch(ConfigMap.class, configName)
            .map(config -> {
                if (config.getData() == null) {
                    return new UserNotificationPreference();
                }
                String s = config.getData().get(NOTIFICATION_PREFERENCE);
                if (StringUtils.isNotBlank(s)) {
                    return JsonUtils.jsonToObject(s, UserNotificationPreference.class);
                }
                return new UserNotificationPreference();
            })
            .defaultIfEmpty(new UserNotificationPreference());
    }

    static String buildUserPreferenceConfigMapName(String username) {
        return "user-preferences-" + username;
    }
}
