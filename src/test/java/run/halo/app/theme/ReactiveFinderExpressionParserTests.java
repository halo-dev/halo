package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import run.halo.app.infra.utils.JsonUtils;
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
        context.setVariable("target", new TestReactiveFinder());
        context.setVariable("genericMap", Map.of("key", "value"));
        String result = templateEngine.process("javascriptInline", context);
        assertThat(result).isEqualTo("""
            <p>value</p>
            <p>ruibaby</p>
            <p>guqing</p>
            <p>bar</p>
            <script>
                var genericValue = "value";
                var name = "guqing";
                var names = ["guqing","johnniang","ruibaby"];
                var users = [{"name":"guqing"},{"name":"ruibaby"},{"name":"johnniang"}];
                var userListItem = "guqing";
                var objectJsonNodeFlux = [{"name":"guqing"}];
                var objectJsonNodeFluxChain = "guqing";
                var mapMono = "bar";
                var arrayNodeMono = "bar";
            </script>
            """);
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

        public Flux<JsonNode> objectJsonNodeFlux() {
            ObjectNode objectNode = JsonUtils.DEFAULT_JSON_MAPPER.createObjectNode();
            objectNode.put("name", "guqing");
            return Flux.just(objectNode);
        }

        public Mono<Map<String, Object>> mapMono() {
            return Mono.just(Map.of("foo", "bar"));
        }

        public Mono<JsonNode> arrayNodeMono() {
            ArrayNode arrayNode = JsonUtils.DEFAULT_JSON_MAPPER.createArrayNode();
            arrayNode.add(arrayNode.objectNode().put("foo", "bar"));
            return Mono.just(arrayNode);
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
                <p th:text="${genericMap.key}"></p>
                <p th:text="${target.users[1].name}"></p>
                <p th:text="${target.objectJsonNodeFlux[0].name}"></p>
                <p th:text="${target.arrayNodeMono.get(0).foo}"></p>
                <script th:inline="javascript">
                    var genericValue = /*[[${genericMap.key}]]*/;
                    var name = /*[[${target.getName()}]]*/;
                    var names = /*[[${target.names()}]]*/;
                    var users = /*[[${target.users()}]]*/;
                    var userListItem = /*[[${target.users[0].name}]]*/;
                    var objectJsonNodeFlux = /*[[${target.objectJsonNodeFlux()}]]*/;
                    var objectJsonNodeFluxChain = /*[[${target.objectJsonNodeFlux[0].name}]]*/;
                    var mapMono = /*[[${target.mapMono.foo}]]*/;
                    var arrayNodeMono = /*[[${target.arrayNodeMono.get(0).foo}]]*/;
                </script>
                 """);
        }

    }
}
