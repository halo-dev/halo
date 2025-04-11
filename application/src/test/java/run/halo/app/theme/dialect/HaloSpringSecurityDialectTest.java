package run.halo.app.theme.dialect;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.http.MediaType.TEXT_HTML;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.extras.springsecurity6.util.SpringSecurityContextUtils;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;
import org.thymeleaf.spring6.web.webflux.SpringWebFluxWebApplication;
import org.thymeleaf.templateresolver.StringTemplateResolver;

// @ExtendWith(MockitoExtension.class)
@SpringBootTest
class HaloSpringSecurityDialectTest {

    TemplateEngine templateEngine;

    @Autowired
    ServerSecurityContextRepository securityContextRepository;

    @Autowired
    ObjectProvider<MethodSecurityExpressionHandler> expressionHandler;


    @BeforeEach
    void setUp() {
        var haloSpringSecurityDialect =
            new HaloSpringSecurityDialect(securityContextRepository, expressionHandler);
        templateEngine = new SpringWebFluxTemplateEngine();
        templateEngine.addTemplateResolver(new StringTemplateResolver());
        templateEngine.addDialect(haloSpringSecurityDialect);
    }

    static Stream<Arguments> shouldEvaluateSecAuthorizeAttr() {
        return Stream.of(
            arguments(
                "Evaluate sec:authorize to true when role match",
                List.of("ROLE_ADMIN"),
                """
                    <p sec:authorize="hasRole('ROLE_ADMIN')">Admin</p>\
                    """,
                """
                    <p>Admin</p>\
                    """),
            arguments(
                "Evaluate sec:authorize to false when role not match",
                List.of("ROLE_USER"),
                """
                    <p sec:authorize="hasRole('ROLE_ADMIN')"></p>\
                    """,
                "")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void shouldEvaluateSecAuthorizeAttr(String name, List<String> authorities, String template,
        String expected) {
        var request = MockServerHttpRequest.get("/halo-sec-authorize").build();
        var exchange = new MockServerWebExchange.Builder(request).build();
        var webExchange = SpringWebFluxWebApplication.buildApplication(null)
            .buildExchange(exchange, Locale.getDefault(), TEXT_HTML, UTF_8);
        var context = new WebContext(webExchange);
        var authentication = new UsernamePasswordAuthenticationToken("fake-user", "fake-credential",
            AuthorityUtils.createAuthorityList(authorities));
        var securityContext = new SecurityContextImpl(authentication);
        context.setVariable(SpringSecurityContextUtils.SECURITY_CONTEXT_MODEL_ATTRIBUTE_NAME,
            securityContext);
        var result = templateEngine.process(template, context);
        assertEquals(expected, result);
    }
}
