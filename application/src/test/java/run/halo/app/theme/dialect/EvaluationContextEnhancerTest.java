package run.halo.app.theme.dialect;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.spring6.dialect.SpringStandardDialect;
import org.thymeleaf.spring6.expression.ThymeleafEvaluationContext;
import org.thymeleaf.templateresolver.StringTemplateResolver;

/**
 * Tests for {@link EvaluationContextEnhancer}.
 *
 * @author ruibaby
 * @since 2.27.0
 */
@ExtendWith(MockitoExtension.class)
class EvaluationContextEnhancerTest {

    @Mock
    private ApplicationContext applicationContext;

    private TemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        templateEngine = new TemplateEngine();
        // Register only the enhancer, so the assertion covers type resolution and not the other
        // processors bundled in HaloProcessorDialect.
        templateEngine.setDialects(Set.of(new EnhancerDialect(), new SpringStandardDialect()));
        templateEngine.addTemplateResolver(new StringTemplateResolver());
    }

    @Test
    void shouldRejectH2TypeReference() {
        var context = new Context();
        context.setVariable(
                ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
                new ThymeleafEvaluationContext(applicationContext, null));

        assertThatThrownBy(() -> templateEngine.process("<p th:text=\"${T(org.h2.Driver)}\"></p>", context))
                .hasMessageContaining("org.h2.Driver");
    }

    @Test
    void shouldAllowOrdinaryTypeReference() {
        var context = new Context();
        context.setVariable(
                ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
                new ThymeleafEvaluationContext(applicationContext, null));

        templateEngine.process("<p th:text=\"${T(java.lang.Math).abs(-1)}\"></p>", context);
    }

    /**
     * Registers {@link EvaluationContextEnhancer} on its own so the test does not depend on the other processors of
     * {@link HaloProcessorDialect}.
     */
    static class EnhancerDialect extends org.thymeleaf.dialect.AbstractProcessorDialect {

        EnhancerDialect() {
            super("enhancer", "halo", org.thymeleaf.standard.StandardDialect.PROCESSOR_PRECEDENCE);
        }

        @Override
        public Set<IProcessor> getProcessors(String dialectPrefix) {
            return Set.of(new EvaluationContextEnhancer());
        }
    }
}
