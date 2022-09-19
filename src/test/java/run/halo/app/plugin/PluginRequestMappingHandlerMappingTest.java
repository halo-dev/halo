package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.get;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginRuntimeException;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for {@link PluginRequestMappingHandlerMapping}.
 *
 * @author guqing
 * @since 2.0.0
 */
class PluginRequestMappingHandlerMappingTest {

    private final StaticWebApplicationContext wac = new StaticWebApplicationContext();

    private PluginRequestMappingHandlerMapping handlerMapping;


    @BeforeEach
    public void setup() {
        handlerMapping = new PluginRequestMappingHandlerMapping();
        this.handlerMapping.setApplicationContext(wac);
    }

    @Test
    public void shouldAddPathPrefixWhenExistingApiVersion() throws Exception {
        Method method = UserController.class.getMethod("getUser");
        RequestMappingInfo info =
            this.handlerMapping.getPluginMappingForMethod("fakePlugin", method,
                UserController.class);

        assertThat(info).isNotNull();
        assertThat(info.getPatternsCondition().getPatterns()).isEqualTo(
            Collections.singleton(
                new PathPatternParser().parse(
                    "/apis/api.plugin.halo.run/v1alpha1/plugins/fakePlugin/user/{id}")));
    }

    @Test
    public void shouldFailWhenMissingApiVersion() throws Exception {
        Method method = AppleMissingApiVersionController.class.getMethod("getName");
        assertThatThrownBy(() ->
            this.handlerMapping.getPluginMappingForMethod("fakePlugin", method,
                AppleMissingApiVersionController.class)).isInstanceOf(PluginRuntimeException.class)
            .hasMessage(
                "The handler [class run.halo.app.plugin"
                    + ".PluginRequestMappingHandlerMappingTest$AppleMissingApiVersionController] "
                    + "is missing @ApiVersion annotation.");
    }

    @Test
    void registerHandlerMethods() {
        assertThat(handlerMapping.getMappings("fakePlugin")).isEmpty();

        UserController userController = mock(UserController.class);
        handlerMapping.registerHandlerMethods("fakePlugin", userController);

        List<RequestMappingInfo> mappings = handlerMapping.getMappings("fakePlugin");
        assertThat(mappings).hasSize(1);
        assertThat(mappings.get(0).toString()).isEqualTo(
            "{GET /apis/api.plugin.halo.run/v1alpha1/plugins/fakePlugin/user/{id}}");
    }

    @Test
    void unregister() {
        UserController userController = mock(UserController.class);
        // register handler methods first
        handlerMapping.registerHandlerMethods("fakePlugin", userController);
        assertThat(handlerMapping.getMappings("fakePlugin")).hasSize(1);

        // unregister
        handlerMapping.unregister("fakePlugin");
        assertThat(handlerMapping.getMappings("fakePlugin")).isEmpty();
    }

    @Test
    public void getHandlerDirectMatch() {
        // register handler methods first
        handlerMapping.registerHandlerMethods("fakePlugin", new TestController());

        // resolve an expected method from TestController
        Method expected =
            ResolvableMethod.on(TestController.class).annot(getMapping("/foo")).build();

        // get handler by mock exchange
        ServerWebExchange exchange =
            MockServerWebExchange.from(
                get("/apis/api.plugin.halo.run/v1alpha1/plugins/fakePlugin/foo"));
        HandlerMethod hm = (HandlerMethod) this.handlerMapping.getHandler(exchange).block();

        assertThat(hm).isNotNull();
        assertThat(hm.getMethod()).isEqualTo(expected);
    }

    @Test
    public void getHandlerBestMatch() {
        // register handler methods first
        handlerMapping.registerHandlerMethods("fakePlugin", new TestController());

        Method expected =
            ResolvableMethod.on(TestController.class).annot(getMapping("/foo").params("p")).build();

        String requestPath = "/apis/api.plugin.halo.run/v1alpha1/plugins/fakePlugin/foo?p=anything";
        ServerWebExchange exchange = MockServerWebExchange.from(get(requestPath));
        HandlerMethod hm = (HandlerMethod) this.handlerMapping.getHandler(exchange).block();

        assertThat(hm).isNotNull();
        assertThat(hm.getMethod()).isEqualTo(expected);
    }

    @Test
    public void getHandlerRootPathMatch() {
        // register handler methods first
        handlerMapping.registerHandlerMethods("fakePlugin", new TestController());
        Method expected =
            ResolvableMethod.on(TestController.class).annot(getMapping("")).build();

        String requestPath = "/apis/api.plugin.halo.run/v1alpha1/plugins/fakePlugin";
        ServerWebExchange exchange = MockServerWebExchange.from(get(requestPath));
        HandlerMethod hm = (HandlerMethod) this.handlerMapping.getHandler(exchange).block();

        assertThat(hm).isNotNull();
        assertThat(hm.getMethod()).isEqualTo(expected);
    }

    @Test
    public void getHandlerRequestMethodNotAllowed() {
        // register handler methods first
        handlerMapping.registerHandlerMethods("fakePlugin", new TestController());

        String requestPath = "/apis/api.plugin.halo.run/v1alpha1/plugins/fakePlugin/bar";
        ServerWebExchange exchange = MockServerWebExchange.from(post(requestPath));
        Mono<Object> mono = this.handlerMapping.getHandler(exchange);

        assertError(mono, MethodNotAllowedException.class,
            ex -> assertThat(ex.getSupportedMethods()).isEqualTo(
                Set.of(HttpMethod.GET, HttpMethod.HEAD)));
    }

    @SuppressWarnings("unchecked")
    private <T> void assertError(Mono<Object> mono, final Class<T> exceptionClass,
        final Consumer<T> consumer) {
        StepVerifier.create(mono)
            .consumeErrorWith(error -> {
                assertThat(error.getClass()).isEqualTo(exceptionClass);
                consumer.accept((T) error);
            })
            .verify();
    }

    private RequestMappingPredicate getMapping(String... path) {
        return new RequestMappingPredicate(path).method(GET).params();
    }

    public static class ResolvableMethod {
        private final Class<?> objectClass;
        private final List<Predicate<Method>> filters = new ArrayList<>(4);

        public ResolvableMethod(Class<?> objectClass) {
            this.objectClass = objectClass;
        }

        public static ResolvableMethod on(Class<?> objectClass) {
            return new ResolvableMethod(objectClass);
        }

        public ResolvableMethod annot(Predicate<Method> predicate) {
            filters.add(predicate);
            return this;
        }

        public Method build() {
            Set<Method> methods = MethodIntrospector.selectMethods(this.objectClass, this::isMatch);
            Assert.state(!methods.isEmpty(), () -> "No matching method: " + this);
            Assert.state(methods.size() == 1,
                () -> "Multiple matching methods: " + this + formatMethods(methods));
            return methods.iterator().next();
        }

        private String formatMethods(Set<Method> methods) {
            return "\nMatched:\n" + methods.stream()
                .map(Method::toGenericString).collect(Collectors.joining(",\n\t", "[\n\t", "\n]"));
        }

        private boolean isMatch(Method method) {
            return this.filters.stream().allMatch(p -> p.test(method));
        }
    }

    public static class RequestMappingPredicate implements Predicate<Method> {

        private final String[] path;

        private RequestMethod[] method = {};

        private String[] params;


        private RequestMappingPredicate(String... path) {
            this.path = path;
        }


        public RequestMappingPredicate method(RequestMethod... methods) {
            this.method = methods;
            return this;
        }

        public RequestMappingPredicate params(String... params) {
            this.params = params;
            return this;
        }

        @Override
        public boolean test(Method method) {
            RequestMapping annot =
                AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            return annot != null
                && Arrays.equals(this.path, annot.path())
                && Arrays.equals(this.method, annot.method())
                && (this.params == null || Arrays.equals(this.params, annot.params()));
        }
    }

    @ApiVersion("v1alpha1")
    @RestController
    @RequestMapping("/user")
    static class UserController {

        @GetMapping("/{id}")
        public Principal getUser() {
            return mock(Principal.class);
        }
    }

    @RestController
    @RequestMapping("/apples")
    static class AppleMissingApiVersionController {

        @GetMapping
        public String getName() {
            return mock(String.class);
        }
    }

    @ApiVersion("v1alpha1")
    @Controller
    @RequestMapping
    static class TestController {
        @GetMapping("/foo")
        public void foo() {
        }

        @GetMapping(path = "/foo", params = "p")
        public void fooParam() {
        }

        @RequestMapping(path = "/ba*", method = {GET, HEAD})
        public void bar() {
        }

        @GetMapping("")
        public void empty() {
        }
    }
}