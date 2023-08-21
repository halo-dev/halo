package run.halo.app.theme.dialect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jsoup.Jsoup;
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
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.plugin.ExtensionComponentsFinder;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.router.ModelConst;

/**
 * Integration tests for {@link ContentTemplateHeadProcessor}.
 *
 * @author guqing
 * @see HaloProcessorDialect
 * @see GlobalHeadInjectionProcessor
 * @see ContentTemplateHeadProcessor
 * @see TemplateHeadProcessor
 * @see TemplateGlobalHeadProcessor
 * @see TemplateFooterElementTagProcessor
 * @since 2.7.0
 */
@ExtendWith(MockitoExtension.class)
class ContentTemplateHeadProcessorIntegrationTest {
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
        map.put("seoProcessor", new GlobalSeoProcessor(fetcher));
        map.put("duplicateMetaTagProcessor", new DuplicateMetaTagProcessor());
        lenient().when(applicationContext.getBeansOfType(eq(TemplateHeadProcessor.class)))
            .thenReturn(map);

        SystemSetting.Seo seo = new SystemSetting.Seo();
        seo.setKeywords("global keywords");
        seo.setDescription("global description");
        lenient().when(fetcher.fetch(eq(SystemSetting.Seo.GROUP), eq(SystemSetting.Seo.class)))
            .thenReturn(Mono.just(seo));

        SystemSetting.CodeInjection codeInjection = new SystemSetting.CodeInjection();
        codeInjection.setGlobalHead(
            "<meta name=\"description\" content=\"global-head-description\"/>");
        codeInjection.setContentHead(
            "<meta name=\"description\" content=\"content-head-description\"/>");
        lenient().when(fetcher.fetch(eq(SystemSetting.CodeInjection.GROUP),
            eq(SystemSetting.CodeInjection.class))).thenReturn(Mono.just(codeInjection));

        lenient().when(applicationContext.getBean(eq(SystemConfigurableEnvironmentFetcher.class)))
            .thenReturn(fetcher);
        lenient().when(fetcher.fetch(eq(SystemSetting.Seo.GROUP), eq(SystemSetting.Seo.class)))
            .thenReturn(Mono.empty());

        lenient().when(applicationContext.getBean(eq(ExtensionComponentsFinder.class)))
            .thenReturn(extensionComponentsFinder);

        lenient().when(extensionComponentsFinder.getExtensions(eq(TemplateHeadProcessor.class)))
            .thenReturn(new ArrayList<>(map.values()));

        lenient().when(applicationContext.getBean(eq(SystemConfigurableEnvironmentFetcher.class)))
            .thenReturn(fetcher);
        lenient().when(fetcher.fetchComment()).thenReturn(Mono.just(new SystemSetting.Comment()));
    }


    @Test
    void overrideGlobalMetaTest() {
        Context context = getContext();
        context.setVariable("name", "fake-post");
        // template id flag is used by TemplateGlobalHeadProcessor
        context.setVariable(ModelConst.TEMPLATE_ID, DefaultTemplateEnum.POST.getValue());

        List<Map<String, String>> htmlMetas = new ArrayList<>();
        htmlMetas.add(mutableMetaMap("keyword", "postK1,postK2"));
        htmlMetas.add(mutableMetaMap("description", "post-description"));
        htmlMetas.add(mutableMetaMap("other", "post-other-meta"));
        Post.PostSpec postSpec = new Post.PostSpec();
        postSpec.setHtmlMetas(htmlMetas);
        Metadata metadata = new Metadata();
        metadata.setName("fake-post");
        PostVo postVo = PostVo.builder().spec(postSpec).metadata(metadata).build();
        when(postFinder.getByName(eq("fake-post"))).thenReturn(Mono.just(postVo));

        String result = templateEngine.process("post", context);
        /*
          this test case shows:
            1. global seo meta keywords and description is overridden by content head meta
            2. global head meta is overridden by content head meta
            3. but global head meta is not overridden by global seo meta
         */
        assertThat(Jsoup.parse(result).html()).isEqualTo("""
            <!doctype html>
            <html lang="en">
             <head>
              <meta charset="UTF-8">
              <title>Post detail</title>
              <meta name="description" content="post-description">
              <meta name="keyword" content="postK1,postK2">
              <meta name="other" content="post-other-meta">
             </head>
             <body>
              this is body
             </body>
            </html>""");
    }

    Map<String, String> mutableMetaMap(String nameValue, String contentValue) {
        Map<String, String> map = new HashMap<>();
        map.put("name", nameValue);
        map.put("content", contentValue);
        return map;
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
            if (template.equals("post")) {
                return new StringTemplateResource(postTemplate());
            }
            return null;
        }

        private String postTemplate() {
            return """
                <!DOCTYPE html>
                <html lang="en" xmlns:th="http://www.thymeleaf.org">
                  <head>
                    <meta charset="UTF-8" />
                    <title>Post detail</title>
                  </head>
                  <body>
                    this is body
                  </body>
                </html>
                """;
        }
    }
}
