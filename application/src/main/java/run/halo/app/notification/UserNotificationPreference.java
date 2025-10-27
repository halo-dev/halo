package run.halo.app.notification;

import static java.util.Objects.requireNonNullElse;

import java.util.HashMap;
import java.util.Set;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.lang.NonNull;

/**
 * Notification preference of user.
 *
 * @author guqing
 * @since 2.10.0
 */
@Data
public class UserNotificationPreference {

    private static final String DEFAULT_NOTIFIER = "default-email-notifier";

    private ReasonTypeNotifier reasonTypeNotifier = new ReasonTypeNotifier();

    public static class ReasonTypeNotifier extends HashMap<String, NotifierSetting> {

        /**
         * Gets notifiers by reason type.
         *
         * @param reasonType reason type
         * @return if key of reasonType not exists, return default notifier, otherwise return the
         * notifiers
         */
        @NonNull
        @JsonIgnore
        public Set<String> getNotifiers(String reasonType) {
            var result = this.get(reasonType);
            return result == null ? Set.of(DEFAULT_NOTIFIER)
                : requireNonNullElse(result.getNotifiers(), Set.of());
        }
    }

    @Data
    public static class NotifierSetting {
        private Set<String> notifiers;
    }
}
