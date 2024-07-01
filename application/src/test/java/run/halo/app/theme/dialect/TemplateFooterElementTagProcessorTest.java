package run.halo.app.theme.dialect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.IProcessor;
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
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/**
 * Tests for {@link TemplateFooterElementTagProcessor}.
 *
 * @author guqing
 * @since 2.17.0
 */
@ExtendWith(MockitoExtension.class)
class TemplateFooterElementTagProcessorTest {
    @Mock
    private ApplicationContext applicationContext;

    @Mock
    ExtensionGetter extensionGetter;

    @Mock
    private SystemConfigurableEnvironmentFetcher fetcher;

    private TemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        HaloProcessorDialect haloProcessorDialect = new MockHaloProcessorDialect();
        templateEngine = new TemplateEngine();
        templateEngine.setDialects(Set.of(haloProcessorDialect, new SpringStandardDialect()));
        templateEngine.addTemplateResolver(new MockTemplateResolver());

        SystemSetting.CodeInjection codeInjection = new SystemSetting.CodeInjection();
        codeInjection.setFooter(
            "<p>Powered by <a href=\"https://www.halo.run\" target=\"_blank\">Halo</a></p>");
        lenient().when(fetcher.fetch(eq(SystemSetting.CodeInjection.GROUP),
            eq(SystemSetting.CodeInjection.class))).thenReturn(Mono.just(codeInjection));

        lenient().when(applicationContext.getBeanProvider(ExtensionGetter.class))
            .thenAnswer(invocation -> {
                var objectProvider = mock(ObjectProvider.class);
                when(objectProvider.getIfUnique()).thenReturn(extensionGetter);
                return objectProvider;
            });
        lenient().when(applicationContext.getBean(eq(SystemConfigurableEnvironmentFetcher.class)))
            .thenReturn(fetcher);
    }

    @Test
    void footerProcessorTest() {
        when(extensionGetter.getExtensions(TemplateFooterProcessor.class))
            .thenReturn(Flux.just(new FakeFooterCodeInjection()));

        String result = templateEngine.process("fake-template", getContext());
        // footer injected code is not processable
        assertThat(result).isEqualToIgnoringWhitespace("""
            <p>Powered by <a href="https://www.halo.run" target="_blank">Halo</a></p>
            <div>© 2024 guqing's blog</div>
            <div th:text="${footerText}"></div>
            """);
    }

    private Context getContext() {
        Context context = new Context();
        context.setVariable(
            ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
            new ThymeleafEvaluationContext(applicationContext, null));
        return context;
    }

    static class MockTemplateResolver extends StringTemplateResolver {
        @Override
        protected ITemplateResource computeTemplateResource(IEngineConfiguration configuration,
            String ownerTemplate, String template,
            Map<String, Object> templateResolutionAttributes) {
            return new StringTemplateResource("""
                <halo:footer />
                """);
        }
    }

    static class MockHaloProcessorDialect extends HaloProcessorDialect {
        @Override
        public Set<IProcessor> getProcessors(String dialectPrefix) {
            var processors = new HashSet<IProcessor>();
            processors.add(new TemplateFooterElementTagProcessor(dialectPrefix));
            return processors;
        }
    }

    static class FakeFooterCodeInjection implements TemplateFooterProcessor {

        @Override
        public Mono<Void> process(ITemplateContext context, IProcessableElementTag tag,
            IElementTagStructureHandler structureHandler, IModel model) {
            var factory = context.getModelFactory();
            // regular footer text
            var copyRight = factory.createText("<div>© 2024 guqing's blog</div>");
            model.add(copyRight);
            // variable footer text
            model.add(factory.createText("<div th:text=\"${footerText}\"></div>"));
            return Mono.empty();
        }
    }
}