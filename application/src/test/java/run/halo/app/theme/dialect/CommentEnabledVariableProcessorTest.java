package run.halo.app.theme.dialect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.WebEngineContext;
import org.thymeleaf.spring6.expression.ThymeleafEvaluationContext;
import org.thymeleaf.web.IWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/**
 * Tests for {@link CommentEnabledVariableProcessor}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class CommentEnabledVariableProcessorTest {
    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ExtensionGetter extensionGetter;

    @Mock
    private SystemConfigurableEnvironmentFetcher environmentFetcher;

    @BeforeEach
    void setUp() {
        lenient().when(applicationContext.getBean(eq(ExtensionGetter.class)))
            .thenReturn(extensionGetter);
    }

    @Test
    void getCommentWidget() {
        when(applicationContext.getBean(eq(SystemConfigurableEnvironmentFetcher.class)))
            .thenReturn(environmentFetcher);
        SystemSetting.Comment commentSetting = mock(SystemSetting.Comment.class);
        when(environmentFetcher.fetchComment())
            .thenReturn(Mono.just(commentSetting));

        CommentWidget commentWidget = mock(CommentWidget.class);
        when(extensionGetter.getEnabledExtensionByDefinition(CommentWidget.class))
            .thenReturn(Flux.just(commentWidget));
        WebEngineContext webContext = mock(WebEngineContext.class);
        var evaluationContext = mock(ThymeleafEvaluationContext.class);
        when(webContext.getVariable(
            eq(ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME)))
            .thenReturn(evaluationContext);
        when(evaluationContext.getApplicationContext()).thenReturn(applicationContext);
        IWebExchange webExchange = mock(IWebExchange.class);
        when(webContext.getExchange()).thenReturn(webExchange);

        // comment disabled
        when(commentSetting.getEnable()).thenReturn(true);
        assertThat(
            CommentEnabledVariableProcessor.getCommentWidget(webContext).isPresent()).isTrue();

        // comment enabled
        when(commentSetting.getEnable()).thenReturn(false);
        assertThat(
            CommentEnabledVariableProcessor.getCommentWidget(webContext).isPresent()).isFalse();

        // comment enabled and ENABLE_COMMENT_ATTRIBUTE is true
        when(commentSetting.getEnable()).thenReturn(true);
        when(webExchange.getAttributeValue(CommentWidget.ENABLE_COMMENT_ATTRIBUTE))
            .thenReturn(true);
        assertThat(
            CommentEnabledVariableProcessor.getCommentWidget(webContext).isPresent()).isTrue();

        // comment enabled and ENABLE_COMMENT_ATTRIBUTE is false
        when(commentSetting.getEnable()).thenReturn(true);
        when(webExchange.getAttributeValue(CommentWidget.ENABLE_COMMENT_ATTRIBUTE))
            .thenReturn(false);
        assertThat(
            CommentEnabledVariableProcessor.getCommentWidget(webContext).isPresent()).isFalse();

        // comment enabled and ENABLE_COMMENT_ATTRIBUTE is null
        when(commentSetting.getEnable()).thenReturn(true);
        when(webExchange.getAttributeValue(CommentWidget.ENABLE_COMMENT_ATTRIBUTE))
            .thenReturn(null);
        assertThat(
            CommentEnabledVariableProcessor.getCommentWidget(webContext).isPresent()).isTrue();

        // comment enabled and ENABLE_COMMENT_ATTRIBUTE is 'false'
        when(commentSetting.getEnable()).thenReturn(true);
        when(webExchange.getAttributeValue(CommentWidget.ENABLE_COMMENT_ATTRIBUTE))
            .thenReturn("false");
        assertThat(
            CommentEnabledVariableProcessor.getCommentWidget(webContext).isPresent()).isFalse();
    }

    @Test
    void populateAllowCommentAttribute() {
        WebEngineContext webContext = mock(WebEngineContext.class);
        IWebExchange webExchange = mock(IWebExchange.class);
        when(webContext.getExchange()).thenReturn(webExchange);

        CommentEnabledVariableProcessor.populateAllowCommentAttribute(webContext, true);
        verify(webExchange).setAttributeValue(
            eq(CommentEnabledVariableProcessor.COMMENT_ENABLED_MODEL_ATTRIBUTE), eq(true));

        CommentEnabledVariableProcessor.populateAllowCommentAttribute(webContext, false);
        verify(webExchange).setAttributeValue(
            eq(CommentEnabledVariableProcessor.COMMENT_ENABLED_MODEL_ATTRIBUTE), eq(false));
    }
}