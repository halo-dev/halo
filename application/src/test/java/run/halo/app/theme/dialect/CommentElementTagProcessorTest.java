package run.halo.app.theme.dialect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.spring6.dialect.SpringStandardDialect;
import org.thymeleaf.spring6.expression.ThymeleafEvaluationContext;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.plugin.ExtensionComponentsFinder;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/**
 * Tests for {@link CommentElementTagProcessor}.
 *
 * @author guqing
 * @see ExtensionComponentsFinder
 * @see HaloProcessorDialect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CommentElementTagProcessorTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ExtensionGetter extensionGetter;

    @Mock
    private SystemConfigurableEnvironmentFetcher environmentFetcher;

    private TemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        HaloProcessorDialect haloProcessorDialect = new HaloProcessorDialect();
        templateEngine = new TemplateEngine();
        templateEngine.setDialects(Set.of(haloProcessorDialect, new SpringStandardDialect()));
        templateEngine.addTemplateResolver(new TestTemplateResolver());
        lenient().when(applicationContext.getBean(eq(ExtensionGetter.class)))
            .thenReturn(extensionGetter);
    }

    @Test
    void doProcess() {
        Context context = getContext();

        when(applicationContext.getBean(eq(SystemConfigurableEnvironmentFetcher.class)))
            .thenReturn(environmentFetcher);
        var commentSetting = mock(SystemSetting.Comment.class);
        when(environmentFetcher.fetchComment())
            .thenReturn(Mono.just(commentSetting));
        when(commentSetting.getEnable()).thenReturn(true);

        when(extensionGetter.getEnabledExtensionByDefinition(eq(CommentWidget.class)))
            .thenReturn(Flux.empty());
        String result = templateEngine.process("commentWidget", context);
        assertThat(result).isEqualTo("""
            <!DOCTYPE html>
            <html lang="en">
              <body>
                <p>comment widget:</p>
               \s
              </body>
            </html>
            """);

        when(extensionGetter.getEnabledExtensionByDefinition(eq(CommentWidget.class)))
            .thenReturn(Flux.just(new DefaultCommentWidget()));
        result = templateEngine.process("commentWidget", context);
        assertThat(result).isEqualTo("""
            <!DOCTYPE html>
            <html lang="en">
              <body>
                <p>comment widget:</p>
                <p>Comment in default widget</p>
              </body>
            </html>
            """);
    }

    static class DefaultCommentWidget implements CommentWidget {

        @Override
        public void render(ITemplateContext context, IProcessableElementTag tag,
            IElementTagStructureHandler structureHandler) {
            structureHandler.replaceWith("<p>Comment in default widget</p>", false);
        }
    }

    private Context getContext() {
        Context context = new Context();
        context.setVariable(
            ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
            new ThymeleafEvaluationContext(applicationContext, null));
        return context;
    }

    static class TestTemplateResolver extends StringTemplateResolver {
        @Override
        protected ITemplateResource computeTemplateResource(IEngineConfiguration configuration,
            String ownerTemplate, String template,
            Map<String, Object> templateResolutionAttributes) {
            if (template.equals("commentWidget")) {
                return new StringTemplateResource(commentWidget());
            }
            return null;
        }

        private String commentWidget() {
            return """
                <!DOCTYPE html>
                <html lang="en">
                  <body>
                    <p>comment widget:</p>
                    <halo:comment/>
                  </body>
                </html>
                """;
        }
    }
}