package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link UserNotificationPreferenceServiceImpl}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class UserNotificationPreferenceServiceImplTest {

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private UserNotificationPreferenceServiceImpl userNotificationPreferenceService;

    @Test
    void getByUser() {
        var configMap = new ConfigMap();
        configMap.setData(Map.of("notification",
            "{\"reasonTypeNotifier\":{\"comment\":{\"notifiers\":[\"test-notifier\"]}}}"));
        when(client.fetch(ConfigMap.class, "user-preferences-guqing"))
            .thenReturn(Mono.just(configMap));
        userNotificationPreferenceService.getByUser("guqing")
            .as(StepVerifier::create)
            .consumeNextWith(preference -> {
                assertThat(preference.getReasonTypeNotifier()).isNotNull();
                assertThat(preference.getReasonTypeNotifier().get("comment")).isNotNull();
                assertThat(preference.getReasonTypeNotifier().get("comment").getNotifiers())
                    .containsExactly("test-notifier");
            })
            .verifyComplete();

        verify(client).fetch(ConfigMap.class, "user-preferences-guqing");
    }

    @Test
    void getByUserWhenNotFound() {
        when(client.fetch(ConfigMap.class, "user-preferences-guqing"))
            .thenReturn(Mono.empty());
        userNotificationPreferenceService.getByUser("guqing")
            .as(StepVerifier::create)
            .consumeNextWith(preference ->
                assertThat(preference.getReasonTypeNotifier()).isNotNull()
            )
            .verifyComplete();

        verify(client).fetch(ConfigMap.class, "user-preferences-guqing");
    }

    @Test
    void getByUserWhenConfigDataNotFound() {
        when(client.fetch(ConfigMap.class, "user-preferences-guqing"))
            .thenReturn(Mono.just(new ConfigMap()));
        userNotificationPreferenceService.getByUser("guqing")
            .as(StepVerifier::create)
            .consumeNextWith(preference ->
                assertThat(preference.getReasonTypeNotifier()).isNotNull()
            )
            .verifyComplete();

        verify(client).fetch(ConfigMap.class, "user-preferences-guqing");
    }


    @Nested
    class UserNotificationPreferenceTest {

        @Test
        void getNotifiers() {
            var preference = new UserNotificationPreference();
            preference.getReasonTypeNotifier().put("comment", null);
            // key doesn't exist
            assertThat(preference.getReasonTypeNotifier().getNotifiers("comment"))
                .containsExactly("default-email-notifier");

            // key exists but the value is null
            preference.getReasonTypeNotifier()
                .put("comment", new UserNotificationPreference.NotifierSetting());
            assertThat(preference.getReasonTypeNotifier().getNotifiers("comment")).isEmpty();

            // key exists and the value is not null
            preference.getReasonTypeNotifier().get("comment").setNotifiers(Set.of("test-notifier"));
            assertThat(preference.getReasonTypeNotifier().getNotifiers("comment"))
                .containsExactly("test-notifier");
        }

        @Test
        void toJson() throws JSONException {
            var preference = new UserNotificationPreference();
            preference.getReasonTypeNotifier().put("comment",
                new UserNotificationPreference.NotifierSetting());
            preference.getReasonTypeNotifier().get("comment").setNotifiers(Set.of("test-notifier"));

            JSONAssert.assertEquals("""
                    {
                      "reasonTypeNotifier": {
                        "comment": {
                            "notifiers": [
                                "test-notifier"
                            ]
                        }
                      }
                    }
                    """,
                JsonUtils.objectToJson(preference),
                true);
        }
    }

    @Test
    void buildUserPreferenceConfigMapName() {
        var preferenceConfigMapName = UserNotificationPreferenceServiceImpl
            .buildUserPreferenceConfigMapName("guqing");
        assertEquals("user-preferences-guqing", preferenceConfigMapName);
    }
}