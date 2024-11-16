package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.dialect.SpringStandardDialect;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.theme.ReactiveSpelVariableExpressionEvaluator;
import run.halo.app.theme.finders.PostPublicQueryService;

/**
 * Tests for {@link PostFinderImpl}.
 *
 * @author guqing
 * @since 2.19.0
 */
@ExtendWith(MockitoExtension.class)
class PostFinderImplIntegrationTest {

    private TemplateEngine templateEngine;

    @Mock
    private PostPublicQueryService postPublicQueryService;

    @InjectMocks
    private PostFinderImpl postFinder;

    @Mock
    private TemplateResourceComputer templateResourceComputer;

    @BeforeEach
    void setUp() {
        templateEngine = new SpringTemplateEngine();
        templateEngine.setDialect(new SpringStandardDialect() {
            @Override
            public IStandardVariableExpressionEvaluator getVariableExpressionEvaluator() {
                return ReactiveSpelVariableExpressionEvaluator.INSTANCE;
            }
        });
        templateEngine.addTemplateResolver(new TestTemplateResolver(templateResourceComputer));
    }

    @Test
    void listTest() {
        var context = new Context();
        context.setVariable("postFinder", postFinder);

        // empty param
        when(templateResourceComputer.compute(eq("post"))).thenReturn(new StringTemplateResource("""
            <span th:text="${postFinder.list({})}"></span>
            """));

        when(postPublicQueryService.list(any(), any()))
            .thenReturn(Mono.just(ListResult.emptyResult()));

        var result = templateEngine.process("post", context);
        assertThat(result).isEqualToIgnoringWhitespace(
            "<span>ListResult(page=0, size=0, total=0, items=[])</span>");

        when(templateResourceComputer.compute(eq("post"))).thenReturn(new StringTemplateResource("""
            <span
              th:each="post : ${postFinder.list({page: 1, size: 10, tagName: 'fake-tag',
               ownerName: 'fake-owner', sort: {'spec.publishTime,desc',
                'metadata.creationTimestamp,asc'}})}"
            >
            </span>
            """));
        result = templateEngine.process("post", context);
        assertThat(result).isEqualToIgnoringWhitespace("");
    }

    static class TestTemplateResolver extends StringTemplateResolver {
        private final TemplateResourceComputer templateResourceComputer;

        TestTemplateResolver(TemplateResourceComputer templateResourceComputer) {
            this.templateResourceComputer = templateResourceComputer;
        }

        @Override
        protected ITemplateResource computeTemplateResource(IEngineConfiguration configuration,
            String ownerTemplate, String template,
            Map<String, Object> templateResolutionAttributes) {
            return templateResourceComputer.compute(template);
        }
    }

    interface TemplateResourceComputer {
        ITemplateResource compute(String template);
    }
}
