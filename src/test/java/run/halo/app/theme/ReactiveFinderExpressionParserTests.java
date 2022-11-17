package run.halo.app.theme;

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
import org.thymeleaf.spring6.dialect.SpringStandardDialect;
import org.thymeleaf.spring6.expression.ThymeleafEvaluationContext;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.theme.dialect.HaloProcessorDialect;

/**
 * Tests expression parser for reactive return value.
 *
 * @author guqing
 * @see ReactivePropertyAccessor
 * @see ReactiveSpelVariableExpressionEvaluator
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
public class ReactiveFinderExpressionParserTests {
    @Mock
    private ApplicationContext applicationContext;

    private TemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        HaloProcessorDialect haloProcessorDialect = new HaloProcessorDialect();
        templateEngine = new TemplateEngine();
        templateEngine.setDialects(Set.of(haloProcessorDialect, new SpringStandardDialect() {
            @Override
            public IStandardVariableExpressionEvaluator getVariableExpressionEvaluator() {
                return new ReactiveSpelVariableExpressionEvaluator();
            }
        }));
        templateEngine.addTemplateResolver(new TestTemplateResolver());
    }

    @Test
    void javascriptInlineParser() {
        Context context = getContext();
        context.setVariable("testReactiveFinder", new TestReactiveFinder());
        String result = templateEngine.process("javascriptInline", context);
        System.out.println(result);
    }

    static class TestReactiveFinder {
        public Mono<String> getName() {
            return Mono.just("guqing");
        }

        public Flux<String> names() {
            return Flux.just("guqing", "johnniang", "ruibaby");
        }

        public Flux<TestUser> users() {
            return Flux.just(
                new TestUser("guqing"), new TestUser("ruibaby"), new TestUser("johnniang")
            );
        }
    }

    record TestUser(String name) {
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
            return new StringTemplateResource("""
                <script th:inline="javascript">
                    var name = /*[[${testReactiveFinder.getName()}]]*/;
                    var names = /*[[${testReactiveFinder.names()}]]*/;
                    var users = /*[[${testReactiveFinder.users()}]]*/;
                    var userListItem = /*[[${testReactiveFinder.users[0]}]]*/;
                </script>
                 """);
        }
    }
}
