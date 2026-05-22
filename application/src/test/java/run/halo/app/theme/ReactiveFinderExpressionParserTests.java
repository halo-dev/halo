package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.spring6.dialect.SpringStandardDialect;
import org.thymeleaf.spring6.expression.ThymeleafEvaluationContext;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.theme.dialect.HaloProcessorDialect;
import run.halo.app.theme.engine.HaloTemplateEngine;

/**
 * Tests expression parser for reactive return value.
 *
 * @author guqing
 * @see ReactiveSpelVariableExpressionEvaluator
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
public class ReactiveFinderExpressionParserTests {
    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ObjectProvider<ExtensionGetter> extensionGetterProvider;

    @Mock
    private SystemConfigFetcher environmentFetcher;

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
        lenient()
                .when(applicationContext.getBean(eq(SystemConfigFetcher.class)))
                .thenReturn(environmentFetcher);
        lenient()
                .when(applicationContext.getBeanProvider(ExtensionGetter.class))
                .thenReturn(extensionGetterProvider);
        lenient().when(extensionGetterProvider.getIfUnique()).thenReturn(null);
        lenient().when(environmentFetcher.fetchComment()).thenReturn(Mono.just(new SystemSetting.Comment()));
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

    @Test
    void finderCallsArePrefetchedBeforeRendering() {
        var engine = new HaloTemplateEngine(new StandardMessageResolver());
        engine.setDialects(Set.of(new HaloProcessorDialect(), new SpringStandardDialect() {
            @Override
            public IStandardVariableExpressionEvaluator getVariableExpressionEvaluator() {
                return new ReactiveSpelVariableExpressionEvaluator();
            }
        }));
        engine.addTemplateResolver(new TestTemplateResolver());

        var finder = new TestConcurrentFinderImpl();
        Context context = getContext();
        var finderPrefetchContext = new FinderPrefetchContext();
        var contextView = reactor.util.context.Context.of(FinderPrefetchContext.CONTEXT_KEY, finderPrefetchContext);
        context.setVariable(HaloViewResolver.HaloView.CONTEXT_VIEW_KEY, contextView);
        assertThat(FinderPrefetchContext.from(context, HaloViewResolver.HaloView.CONTEXT_VIEW_KEY))
                .containsSame(finderPrefetchContext);
        var finderProxy = FinderPrefetchProxyFactory.create("target", finder, finderPrefetchContext, contextView);
        assertThat(Proxy.isProxyClass(finderProxy.getClass())).isTrue();
        context.setVariable("target", finderProxy);

        var publisher = engine.processStream(
                "prefetch",
                Set.of(),
                context,
                new DefaultDataBufferFactory(),
                MediaType.TEXT_HTML,
                StandardCharsets.UTF_8,
                Integer.MAX_VALUE);
        var result = Flux.from(publisher)
                .map(buffer -> {
                    try {
                        return buffer.toString(StandardCharsets.UTF_8);
                    } finally {
                        DataBufferUtils.release(buffer);
                    }
                })
                .collectList()
                .map(items -> String.join("", items))
                .block(Duration.ofSeconds(5));

        assertThat(result).isEqualTo("""
            <p>one</p>
            <p>two</p>
            """);
        assertThat(finder.subscriptions).hasValue(2);
    }

    @Test
    void finalRenderAfterAsyncPrefetchRunsOnBlockingScheduler() {
        var engine = new HaloTemplateEngine(new StandardMessageResolver());
        engine.setDialects(Set.of(new HaloProcessorDialect(), new SpringStandardDialect() {
            @Override
            public IStandardVariableExpressionEvaluator getVariableExpressionEvaluator() {
                return new ReactiveSpelVariableExpressionEvaluator();
            }
        }));
        engine.addTemplateResolver(new TestTemplateResolver());

        Context context = getContext();
        var finderPrefetchContext = new FinderPrefetchContext();
        var contextView = reactor.util.context.Context.of(FinderPrefetchContext.CONTEXT_KEY, finderPrefetchContext);
        context.setVariable(HaloViewResolver.HaloView.CONTEXT_VIEW_KEY, contextView);
        context.setVariable(
                "target",
                FinderPrefetchProxyFactory.create(
                        "target", new NonBlockingCompletionFinder(), finderPrefetchContext, contextView));

        var publisher = engine.processStream(
                "prefetch",
                Set.of(),
                context,
                new DefaultDataBufferFactory(),
                MediaType.TEXT_HTML,
                StandardCharsets.UTF_8,
                Integer.MAX_VALUE);
        var result = Flux.from(publisher)
                .map(buffer -> {
                    try {
                        return buffer.toString(StandardCharsets.UTF_8);
                    } finally {
                        DataBufferUtils.release(buffer);
                    }
                })
                .collectList()
                .map(items -> String.join("", items))
                .block(Duration.ofSeconds(5));

        assertThat(result).isEqualTo("""
            <p>one</p>
            <p>two</p>
            """);
    }

    @Test
    void finderProxyRecordsCallsDuringDiscovery() {
        var finder = new TestConcurrentFinderImpl();
        var finderPrefetchContext = new FinderPrefetchContext();
        var contextView = reactor.util.context.Context.of(FinderPrefetchContext.CONTEXT_KEY, finderPrefetchContext);
        var finderProxy = (TestConcurrentFinder)
                FinderPrefetchProxyFactory.create("target", finder, finderPrefetchContext, contextView);

        finderPrefetchContext.startDiscovery();
        finderProxy.first().contextWrite(contextView).blockOptional(Duration.ofSeconds(1));
        finderProxy.second().contextWrite(contextView).blockOptional(Duration.ofSeconds(1));
        assertThat(finder.subscriptions).hasValue(0);

        finderPrefetchContext.awaitPrefetchedValues().block(Duration.ofSeconds(5));

        assertThat(finder.subscriptions).hasValue(2);
    }

    static class TestReactiveFinder {
        public Mono<String> getName() {
            return Mono.just("guqing");
        }

        public Flux<String> names() {
            return Flux.just("guqing", "johnniang", "ruibaby");
        }

        public Flux<TestUser> users() {
            return Flux.just(new TestUser("guqing"), new TestUser("ruibaby"), new TestUser("johnniang"));
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

    record TestUser(String name) {}

    interface TestConcurrentFinder {
        Mono<String> first();

        Mono<String> second();
    }

    static class TestConcurrentFinderImpl implements TestConcurrentFinder {
        private final AtomicInteger subscriptions = new AtomicInteger();
        private final CountDownLatch bothSubscribed = new CountDownLatch(1);

        @Override
        public Mono<String> first() {
            return waitForBothSubscriptions("one");
        }

        @Override
        public Mono<String> second() {
            return waitForBothSubscriptions("two");
        }

        private Mono<String> waitForBothSubscriptions(String value) {
            return Mono.<String>create(sink -> {
                if (subscriptions.incrementAndGet() == 2) {
                    bothSubscribed.countDown();
                }
                Schedulers.boundedElastic().schedule(() -> {
                    try {
                        if (bothSubscribed.await(2, TimeUnit.SECONDS)) {
                            sink.success(value);
                        } else {
                            sink.error(new AssertionError("Finder calls were not subscribed concurrently"));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        sink.error(e);
                    }
                });
            });
        }
    }

    static class NonBlockingCompletionFinder implements TestConcurrentFinder {
        @Override
        public Mono<String> first() {
            return Mono.delay(Duration.ofMillis(10)).thenReturn("one");
        }

        @Override
        public Mono<String> second() {
            return Mono.delay(Duration.ofMillis(10)).thenReturn("two");
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
        protected ITemplateResource computeTemplateResource(
                IEngineConfiguration configuration,
                String ownerTemplate,
                String template,
                Map<String, Object> templateResolutionAttributes) {
            if ("prefetch".equals(template)) {
                return new StringTemplateResource("""
                    <p th:text="${target.first()}"></p>
                    <p th:text="${target.second()}"></p>
                    """);
            }
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
