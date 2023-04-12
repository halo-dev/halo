package run.halo.app.theme.dialect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSortedMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.plugin.ExtensionComponentsFinder;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.finders.vo.UserVo;
import run.halo.app.theme.router.factories.ModelConst;

/**
 * Tests for {@link HaloProcessorDialect}.
 *
 * @author guqing
 * @see HaloProcessorDialect
 * @see GlobalHeadInjectionProcessor
 * @see ContentTemplateHeadProcessor
 * @see TemplateHeadProcessor
 * @see TemplateGlobalHeadProcessor
 * @see TemplateFooterElementTagProcessor
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class HaloProcessorDialectTest {
    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private PostFinder postFinder;

    @Mock
    private SinglePageFinder singlePageFinder;

    @Mock
    private SystemConfigurableEnvironmentFetcher fetcher;

    @Mock
    private ExtensionComponentsFinder extensionComponentsFinder;

    private TemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        HaloProcessorDialect haloProcessorDialect = new HaloProcessorDialect();
        templateEngine = new TemplateEngine();
        templateEngine.setDialects(Set.of(haloProcessorDialect, new SpringStandardDialect()));
        templateEngine.addTemplateResolver(new TestTemplateResolver());

        Map<String, TemplateHeadProcessor> map = new HashMap<>();
        map.put("postTemplateHeadProcessor",
            new ContentTemplateHeadProcessor(postFinder, singlePageFinder));
        map.put("templateGlobalHeadProcessor", new TemplateGlobalHeadProcessor(fetcher));
        map.put("faviconHeadProcessor", new DefaultFaviconHeadProcessor(fetcher));
        map.put("globalSeoProcessor", new GlobalSeoProcessor(fetcher));
        lenient().when(applicationContext.getBeansOfType(eq(TemplateHeadProcessor.class)))
            .thenReturn(map);

        SystemSetting.CodeInjection codeInjection = new SystemSetting.CodeInjection();
        codeInjection.setContentHead("<meta name=\"content-head-test\" content=\"test\" />");
        codeInjection.setGlobalHead("<meta name=\"global-head-test\" content=\"test\" />");
        codeInjection.setFooter("<footer>hello this is global footer.</footer>");
        lenient().when(fetcher.fetch(eq(SystemSetting.CodeInjection.GROUP),
            eq(SystemSetting.CodeInjection.class))).thenReturn(Mono.just(codeInjection));

        lenient().when(applicationContext.getBean(eq(SystemConfigurableEnvironmentFetcher.class)))
            .thenReturn(fetcher);
        lenient().when(fetcher.fetch(eq(SystemSetting.Seo.GROUP),
            eq(SystemSetting.Seo.class))).thenReturn(Mono.empty());

        lenient().when(applicationContext.getBean(eq(ExtensionComponentsFinder.class)))
            .thenReturn(extensionComponentsFinder);

        lenient().when(extensionComponentsFinder.getExtensions(eq(TemplateHeadProcessor.class)))
            .thenReturn(new ArrayList<>(map.values()));
    }

    @Test
    void globalHeadAndFooterProcessors() {
        SystemSetting.Basic basic = new SystemSetting.Basic();
        basic.setFavicon("favicon.ico");
        when(fetcher.fetch(eq(SystemSetting.Basic.GROUP),
            eq(SystemSetting.Basic.class))).thenReturn(Mono.just(basic));

        Context context = getContext();

        String result = templateEngine.process("index", context);
        assertThat(result).isEqualTo("""
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8" />
                <title>Index</title>
              <meta name="global-head-test" content="test" />
            <link rel="icon" href="favicon.ico" />
            </head>
              <body>
                <p>index</p>
            <div class="footer">
              <footer>hello this is global footer.</footer>
            </div>
                        
              </body>
            </html>
            """);
    }

    @Test
    void contentHeadAndFooterAndPostProcessors() {
        Context context = getContext();
        context.setVariable("name", "fake-post");
        // template id flag is used by TemplateGlobalHeadProcessor
        context.setVariable(ModelConst.TEMPLATE_ID, DefaultTemplateEnum.POST.getValue());

        List<Map<String, String>> htmlMetas = new ArrayList<>();
        htmlMetas.add(ImmutableSortedMap.of("name", "post-meta-V1", "content", "post-meta-V1"));
        htmlMetas.add(ImmutableSortedMap.of("name", "post-meta-V2", "content", "post-meta-V2"));
        Post.PostSpec postSpec = new Post.PostSpec();
        postSpec.setHtmlMetas(htmlMetas);
        Metadata metadata = new Metadata();
        metadata.setName("fake-post");
        PostVo postVo = PostVo.builder()
            .spec(postSpec)
            .metadata(metadata).build();
        when(postFinder.getByName(eq("fake-post"))).thenReturn(Mono.just(postVo));

        SystemSetting.Basic basic = new SystemSetting.Basic();
        basic.setFavicon(null);
        when(fetcher.fetch(eq(SystemSetting.Basic.GROUP),
            eq(SystemSetting.Basic.class))).thenReturn(Mono.just(basic));

        String result = templateEngine.process("post", context);
        assertThat(result).isEqualTo("""
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8" />
                <title>Post</title>
              <meta content="post-meta-V1" name="post-meta-V1" />
            <meta content="post-meta-V2" name="post-meta-V2" />
            <meta name="description" content="" />
            <meta name="global-head-test" content="test" />
            <meta name="content-head-test" content="test" />
            </head>
              <body>
                <p>post</p>
            <div class="footer">
              <footer>hello this is global footer.</footer>
            </div>
                       
              </body>
            </html>
            """);
    }

    @Test
    void blockSeo() {
        final Context context = getContext();
        SystemSetting.Seo seo = new SystemSetting.Seo();
        seo.setBlockSpiders(true);
        when(fetcher.fetch(eq(SystemSetting.Seo.GROUP),
            eq(SystemSetting.Seo.class))).thenReturn(Mono.just(seo));
        SystemSetting.Basic basic = new SystemSetting.Basic();
        basic.setFavicon("favicon.ico");
        when(fetcher.fetch(eq(SystemSetting.Basic.GROUP),
            eq(SystemSetting.Basic.class))).thenReturn(Mono.just(basic));

        String result = templateEngine.process("seo", context);
        assertThat(result).isEqualTo("""
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8" />
                <title>Seo Test</title>
              <meta name="global-head-test" content="test" />
            <link rel="icon" href="favicon.ico" />
            <meta name="robots" content="noindex" />
            </head>
              <body>
                seo setting test.
              </body>
            </html>
            """);
    }

    @Test
    void seoWithKeywordsAndDescription() {
        final Context context = getContext();
        SystemSetting.Seo seo = new SystemSetting.Seo();
        seo.setKeywords("K1, K2, K3");
        seo.setDescription("This is a description.");
        when(fetcher.fetch(eq(SystemSetting.Seo.GROUP),
            eq(SystemSetting.Seo.class))).thenReturn(Mono.just(seo));
        SystemSetting.Basic basic = new SystemSetting.Basic();
        basic.setFavicon("favicon.ico");
        when(fetcher.fetch(eq(SystemSetting.Basic.GROUP),
            eq(SystemSetting.Basic.class))).thenReturn(Mono.just(basic));

        String result = templateEngine.process("seo", context);
        assertThat(result).isEqualTo("""
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8" />
                <title>Seo Test</title>
              <meta name="global-head-test" content="test" />
            <link rel="icon" href="favicon.ico" />
            <meta name="keywords" content="K1, K2, K3" />
            <meta name="description" content="This is a description." />
            </head>
              <body>
                seo setting test.
              </body>
            </html>
            """);
    }

    @Nested
    class AnnotationExpressionObjectFactoryTest {

        @Test
        void getWhenAnnotationsIsNull() {
            Context context = getContext();
            context.setVariable("user", createUser());

            String result = templateEngine.process("annotationsGetExpression", context);
            assertThat(result).isEqualTo("<p></p>\n");
        }

        @Test
        void getWhenAnnotationsExists() {
            Context context = getContext();
            UserVo user = createUser();
            user.getMetadata().setAnnotations(Map.of("background", "fake-background"));
            context.setVariable("user", user);

            String result = templateEngine.process("annotationsGetExpression", context);
            assertThat(result).isEqualTo("<p>fake-background</p>\n");
        }

        @Test
        void getOrDefaultWhenAnnotationsIsNull() {
            Context context = getContext();
            UserVo user = createUser();
            user.getMetadata().setAnnotations(Map.of("background", "red"));
            context.setVariable("user", user);

            String result = templateEngine.process("annotationsGetOrDefaultExpression", context);
            assertThat(result).isEqualTo("<p>red</p>\n");
        }

        @Test
        void getOrDefaultWhenAnnotationsExists() {
            Context context = getContext();
            context.setVariable("user", createUser());

            String result = templateEngine.process("annotationsGetOrDefaultExpression", context);
            assertThat(result).isEqualTo("<p>default-value</p>\n");
        }

        @Test
        void containsWhenAnnotationsIsNull() {
            Context context = getContext();
            context.setVariable("user", createUser());

            String result = templateEngine.process("annotationsContainsExpression", context);
            assertThat(result).isEqualTo("<p>false</p>\n");
        }

        @Test
        void containsWhenAnnotationsIsNotNull() {
            Context context = getContext();
            UserVo user = createUser();
            user.getMetadata().setAnnotations(Map.of("background", ""));
            context.setVariable("user", user);

            String result = templateEngine.process("annotationsContainsExpression", context);
            assertThat(result).isEqualTo("<p>true</p>\n");
        }

        UserVo createUser() {
            User user = new User();
            user.setMetadata(new Metadata());
            user.getMetadata().setName("fake-user");
            user.setSpec(new User.UserSpec());
            return UserVo.from(user);
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
            if (template.equals(DefaultTemplateEnum.INDEX.getValue())) {
                return new StringTemplateResource(indexTemplate());
            }

            if (template.equals(DefaultTemplateEnum.POST.getValue())) {
                return new StringTemplateResource(postTemplate());
            }

            if (template.equals("seo")) {
                return new StringTemplateResource(seoTemplate());
            }

            if (template.equals("annotationsGetExpression")) {
                return new StringTemplateResource(annotationsGetExpression());
            }
            if (template.equals("annotationsGetOrDefaultExpression")) {
                return new StringTemplateResource(annotationsGetOrDefaultExpression());
            }
            if (template.equals("annotationsContainsExpression")) {
                return new StringTemplateResource(annotationsContainsExpression());
            }
            return null;
        }

        private String indexTemplate() {
            return commonTemplate().formatted("Index", """
                <p>index</p>
                <div class="footer">
                  <halo:footer></halo:footer>
                </div>
                """);
        }

        private String postTemplate() {
            return commonTemplate().formatted("Post", """
                <p>post</p>
                <div class="footer">
                  <halo:footer></halo:footer>
                </div>
                """);
        }

        private String commonTemplate() {
            return """
                <!DOCTYPE html>
                <html lang="en" xmlns:th="http://www.thymeleaf.org">
                  <head>
                    <meta charset="UTF-8" />
                    <title>%s</title>
                  </head>
                  <body>
                    %s
                  </body>
                </html>
                """;
        }

        private String seoTemplate() {
            return """
                <!DOCTYPE html>
                <html lang="en" xmlns:th="http://www.thymeleaf.org">
                  <head>
                    <meta charset="UTF-8" />
                    <title>Seo Test</title>
                  </head>
                  <body>
                    seo setting test.
                  </body>
                </html>
                """;
        }

        private String annotationsGetExpression() {
            return """
                <p th:text="${#annotations.get(user, 'background')}"></p>
                """;
        }

        private String annotationsGetOrDefaultExpression() {
            return """
                <p th:text="${#annotations.getOrDefault(user, 'background', 'default-value')}"></p>
                """;
        }

        private String annotationsContainsExpression() {
            return """
                <p th:text="${#annotations.contains(user, 'background')}"></p>
                """;
        }
    }
}