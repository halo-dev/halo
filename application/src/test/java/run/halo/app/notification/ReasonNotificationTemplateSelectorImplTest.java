package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.notification.NotificationTemplate;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link ReasonNotificationTemplateSelectorImpl}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class ReasonNotificationTemplateSelectorImplTest {

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    ReasonNotificationTemplateSelectorImpl templateSelector;

    @Test
    void select() {
        when(client.listAll(eq(NotificationTemplate.class), any(), any(Sort.class)))
            .thenReturn(Flux.fromIterable(templates()));
        // language priority: zh_CN -> zh -> default
        // if language is same, then compare creationTimestamp to get the latest one
        templateSelector.select("new-comment-on-post", Locale.SIMPLIFIED_CHINESE)
            .as(StepVerifier::create)
            .consumeNextWith(template -> {
                assertThat(template.getMetadata().getName()).isEqualTo("template-2");
                assertThat(template.getSpec().getTemplate().getTitle()).isEqualTo("B");
            })
            .verifyComplete();
    }

    @Test
    void lookupTemplateByLocaleTest() {
        Map<String, Optional<NotificationTemplate>> map = new HashMap<>();
        map.put("zh_CN", Optional.of(createNotificationTemplate("zh_CN-template")));
        map.put("zh", Optional.of(createNotificationTemplate("zh-template")));
        map.put("default", Optional.of(createNotificationTemplate("default-template")));

        var sc = ReasonNotificationTemplateSelectorImpl
            .lookupTemplateByLocale(Locale.SIMPLIFIED_CHINESE, map);
        assertThat(sc).isNotNull();
        assertThat(sc.getMetadata().getName()).isEqualTo("zh_CN-template");

        var c = ReasonNotificationTemplateSelectorImpl
            .lookupTemplateByLocale(Locale.CHINESE, map);
        assertThat(c).isNotNull();
        assertThat(c.getMetadata().getName()).isEqualTo("zh-template");

        var e = ReasonNotificationTemplateSelectorImpl
            .lookupTemplateByLocale(Locale.ENGLISH, map);
        assertThat(e).isNotNull();
        assertThat(e.getMetadata().getName()).isEqualTo("default-template");
    }

    @Test
    void matchReasonTypeTest() {
        var template = createNotificationTemplate("fake-template");
        assertThat(ReasonNotificationTemplateSelectorImpl.matchReasonType("new-comment-on-post")
            .test(template)).isTrue();

        assertThat(ReasonNotificationTemplateSelectorImpl.matchReasonType("fake-reason-type")
            .test(template)).isFalse();
    }

    @Test
    void getLanguageKeyTest() {
        final var languageKeyFunc = ReasonNotificationTemplateSelectorImpl.getLanguageKey();
        var template = createNotificationTemplate("fake-template");
        assertThat(languageKeyFunc.apply(template)).isEqualTo("zh_CN");

        template.getSpec().getReasonSelector().setLanguage("");
        template.getSpec().getReasonSelector().setReasonType("new-comment-on-post");
        assertThat(languageKeyFunc.apply(template)).isEqualTo("default");
    }

    @NonNull
    private static NotificationTemplate createNotificationTemplate(String name) {
        var template = new NotificationTemplate();
        template.setMetadata(new Metadata());
        template.getMetadata().setName(name);
        template.setSpec(new NotificationTemplate.Spec());
        template.getSpec().setReasonSelector(new NotificationTemplate.ReasonSelector());
        template.getSpec().getReasonSelector().setLanguage("zh_CN");
        template.getSpec().getReasonSelector().setReasonType("new-comment-on-post");
        return template;
    }

    List<NotificationTemplate> templates() {
        return Stream.of("""
                    {
                        "apiVersion": "notification.halo.run/v1alpha1",
                        "kind": "NotificationTemplate",
                        "metadata": {
                            "name": "template-1",
                            "creationTimestamp": "2023-01-01T00:00:00Z"
                        },
                        "spec": {
                            "reasonSelector": {
                                "language": "zh",
                                "reasonType": "new-comment-on-post"
                            },
                            "template": {
                                "body": "",
                                "title": "A"
                            }
                        }
                    }
                    """,
                """
                    {
                        "apiVersion": "notification.halo.run/v1alpha1",
                        "kind": "NotificationTemplate",
                        "metadata": {
                            "name": "template-2",
                            "creationTimestamp": "2023-01-01T00:00:03Z"
                        },
                        "spec": {
                            "reasonSelector": {
                                "language": "zh_CN",
                                "reasonType": "new-comment-on-post"
                            },
                            "template": {
                                "body": "",
                                "title": "B"
                            }
                        }
                    }
                    """,
                """
                    {
                        "apiVersion": "notification.halo.run/v1alpha1",
                        "kind": "NotificationTemplate",
                        "metadata": {
                            "name": "template-3",
                            "creationTimestamp": "2023-01-01T00:00:00Z"
                        },
                        "spec": {
                            "reasonSelector": {
                                "language": "zh_CN",
                                "reasonType": "new-comment-on-post"
                            },
                            "template": {
                                "body": "",
                                "title": "C"
                            }
                        }
                    }
                    """)
            .map(json -> JsonUtils.jsonToObject(json, NotificationTemplate.class))
            .toList();
    }
}
