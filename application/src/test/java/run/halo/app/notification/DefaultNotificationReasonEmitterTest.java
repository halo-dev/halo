package run.halo.app.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.ReasonType;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link DefaultNotificationReasonEmitter}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultNotificationReasonEmitterTest {

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private DefaultNotificationReasonEmitter emitter;

    @Test
    void testEmitWhenReasonTypeNotFound() {
        var reasonType = createReasonType();
        when(client.fetch(eq(ReasonType.class), eq(reasonType.getMetadata().getName())))
            .thenReturn(Mono.empty());
        doEmmit(reasonType, reasonAttributes())
            .as(StepVerifier::create)
            .verifyErrorMessage("404 NOT_FOUND \"ReasonType [" + reasonType.getMetadata().getName()
                + "] not found, do you forget to register it?\"");
    }

    @Test
    void testEmitWhenMissingAttributeValue() {
        var reasonType = createReasonType();
        when(client.fetch(eq(ReasonType.class), eq(reasonType.getMetadata().getName())))
            .thenReturn(Mono.just(reasonType));

        var map = reasonAttributes();
        map.put("commenter", null);
        doEmmit(reasonType, map)
            .as(StepVerifier::create)
            .verifyErrorMessage("Reason property [commenter] is required.");
    }

    @Test
    void testEmitWhenMissingOptionalAttribute() {
        var reasonType = createReasonType();
        when(client.fetch(eq(ReasonType.class), eq(reasonType.getMetadata().getName())))
            .thenReturn(Mono.just(reasonType));

        when(client.create(any(Reason.class))).thenReturn(Mono.empty());

        var map = reasonAttributes();
        map.put("postTitle", null);
        doEmmit(reasonType, map)
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    void testCreateReasonOnEmit() {
        var reasonType = createReasonType();
        when(client.fetch(eq(ReasonType.class), eq(reasonType.getMetadata().getName())))
            .thenReturn(Mono.just(reasonType));

        when(client.create(any(Reason.class))).thenReturn(Mono.empty());

        var spyEmitter = spy(emitter);
        doAnswer(as -> {
            var returnedValue = as.callRealMethod();
            JSONAssert.assertEquals(createReasonJson(),
                JsonUtils.objectToJson(returnedValue), true);
            return returnedValue;
        }).when(spyEmitter).createReason(any(), any());

        spyEmitter.emit(reasonType.getMetadata().getName(),
                builder -> builder.attributes(reasonAttributes())
                    .subject(Reason.Subject.builder()
                        .apiVersion("content.halo.run/v1alpha1")
                        .kind("Post")
                        .name("5152aea5-c2e8-4717-8bba-2263d46e19d5")
                        .title("Hello Halo")
                        .url("/archives/hello-halo")
                        .build()
                    )
            )
            .as(StepVerifier::create)
            .verifyComplete();
    }

    Map<String, Object> reasonAttributes() {
        var map = new LinkedHashMap<String, Object>();
        map.put("postName", "5152aea5-c2e8-4717-8bba-2263d46e19d5");
        map.put("postTitle", "Hello Halo");
        map.put("commenter", "guqing");
        map.put("commentName", "53a76c38-5df2-469d-ae1b-68f5ae21a398");
        map.put("content", "测试评论");
        return map;
    }

    private Mono<Void> doEmmit(ReasonType reasonType, Map<String, Object> map) {
        return emitter.emit(reasonType.getMetadata().getName(), builder -> {
            builder.attributes(map)
                .subject(Reason.Subject.builder()
                    .apiVersion("content.halo.run/v1alpha1")
                    .kind("Post")
                    .name("5152aea5-c2e8-4717-8bba-2263d46e19d5")
                    .title("Hello Halo")
                    .url("/archives/hello-halo")
                    .build()
                );
        });
    }

    String createReasonJson() {
        return """
            {
                "spec": {
                    "reasonType": "new-comment-on-post",
                    "subject": {
                        "apiVersion": "content.halo.run/v1alpha1",
                        "kind": "Post",
                        "name": "5152aea5-c2e8-4717-8bba-2263d46e19d5",
                        "title": "Hello Halo",
                        "url": "/archives/hello-halo"
                    },
                    "attributes": {
                        "postName": "5152aea5-c2e8-4717-8bba-2263d46e19d5",
                        "postTitle": "Hello Halo",
                        "commentName": "53a76c38-5df2-469d-ae1b-68f5ae21a398",
                        "content": "测试评论",
                        "commenter": "guqing"
                    }
                },
                "apiVersion": "notification.halo.run/v1alpha1",
                "kind": "Reason",
                "metadata": {
                    "generateName": "reason-"
                }
            }
            """;
    }

    ReasonType createReasonType() {
        return JsonUtils.jsonToObject("""
            {
                "apiVersion": "notification.halo.run/v1alpha1",
                "kind": "ReasonType",
                "metadata": {
                    "name": "new-comment-on-post"
                },
                "spec": {
                    "description": "当你的文章收到新评论时，触发事件",
                    "displayName": "文章收到新评论",
                    "properties": [
                        {
                            "name": "postName",
                            "type": "string"
                        },
                        {
                            "name": "postTitle",
                            "type": "string",
                            "optional": true
                        },
                        {
                            "name": "commenter",
                            "type": "string"
                        },
                        {
                            "name": "commentName",
                            "type": "string"
                        },
                        {
                            "name": "content",
                            "type": "string"
                        }
                    ]
                }
            }
            """, ReasonType.class);
    }
}
