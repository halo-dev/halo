package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link UserNotificationPreference}.
 *
 * @author guqing
 * @since 2.9.0
 */
class UserNotificationPreferenceTest {

    @Test
    void preferenceCreation() {
        String s = """
            {
              "reasonTypeNotifier": {
                "comment": {
                  "notifiers": [
                    "email-notifier",
                    "sms-notifier"
                  ]
                },
                "new-post": {
                  "notifiers": [
                    "email-notifier",
                    "webhook-router-notifier"
                  ]
                }
              }
            }
            """;
        var preference = JsonUtils.jsonToObject(s, UserNotificationPreference.class);
        assertThat(preference.getReasonTypeNotifier()).isNotNull();
        assertThat(preference.getReasonTypeNotifier().get("comment").getNotifiers())
            .containsExactlyInAnyOrder("email-notifier", "sms-notifier");
        assertThat(preference.getReasonTypeNotifier().get("new-post").getNotifiers())
            .containsExactlyInAnyOrder("email-notifier", "webhook-router-notifier");
    }
}
