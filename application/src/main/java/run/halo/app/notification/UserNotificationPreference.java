package run.halo.app.notification;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.HashMap;
import java.util.Set;
import lombok.Data;
import lombok.Getter;

/**
 * Notification preference of user.
 *
 * @author guqing
 * @since 2.10.0
 */
@Getter
public class UserNotificationPreference {
    private static final String DEFAULT_NOTIFIER = "default-email-notifier";

    private final ReasonTypeNotifier reasonTypeNotifier = new ReasonTypeNotifier();

    public static class ReasonTypeNotifier extends HashMap<String, NotifierSetting> {

        /**
         * Gets notifiers by reason type.
         *
         * @param reasonType reason type
         * @return if key of reasonType not exists, return default notifier, otherwise return the
         * notifiers
         */
        public Set<String> getNotifiers(String reasonType) {
            var result = this.get(reasonType);
            return result == null ? Set.of(DEFAULT_NOTIFIER)
                : defaultIfNull(result.getNotifiers(), Set.of());
        }
    }

    @Data
    public static class NotifierSetting {
        private Set<String> notifiers;
    }
}
