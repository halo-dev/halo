package run.halo.app.notification;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;

/**
 * Tests for {@link DefaultNotificationTemplateRender}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultNotificationTemplateRenderTest {

    @Mock
    private SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Mock
    private ExternalUrlSupplier externalUrlSupplier;

    @InjectMocks
    DefaultNotificationTemplateRender templateRender;

    @BeforeEach
    void setUp() throws MalformedURLException {
        var uri = URI.create("http://localhost:8090");
        lenient().when(externalUrlSupplier.get()).thenReturn(uri);
        lenient().when(externalUrlSupplier.getRaw()).thenReturn(uri.toURL());
    }

    @Test
    void render() {
        final String template = """
            亲爱的博主

              [(${replier})] 在评论“[(${isQuoteReply ? quoteContent : commentContent})]”中回复了您，
              以下是回复的具体内容：
              
              [(${content})]

            [(${site.title})]
            [(${site.url})]
            祝好！
            查看原文：[(${commentSubjectUrl})]
            """;
        final var model = Map.<String, Object>of(
            "replier", "guqing",
            "isQuoteReply", true,
            "quoteContent", "这是引用的内容",
            "commentContent", "这是评论的内容",
            "commentSubjectUrl", "/archives/1",
            "content", "这是回复的内容"
        );

        var basic = new SystemSetting.Basic();
        basic.setTitle("Halo");
        basic.setLogo("https://halo.run/logo");
        basic.setSubtitle("Halo");
        when(environmentFetcher.fetch(eq(SystemSetting.Basic.GROUP), eq(SystemSetting.Basic.class)))
            .thenReturn(Mono.just(basic));

        templateRender.render(template, model)
            .as(StepVerifier::create)
            .consumeNextWith(render -> {
                assertThat(render).isEqualTo("""
                    亲爱的博主

                      guqing 在评论“这是引用的内容”中回复了您，
                      以下是回复的具体内容：
                      
                      这是回复的内容
   
                    Halo
                    http://localhost:8090
                    祝好！
                    查看原文：/archives/1
                    """);
            })
            .verifyComplete();

        verify(environmentFetcher).fetch(eq(SystemSetting.Basic.GROUP),
            eq(SystemSetting.Basic.class));
        verify(externalUrlSupplier).getRaw();
    }
}
