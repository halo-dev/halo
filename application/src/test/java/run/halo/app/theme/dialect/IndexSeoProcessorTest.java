package run.halo.app.theme.dialect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.AttributeValueQuotes;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.templatemode.TemplateMode;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.finders.vo.SiteSettingVo;
import run.halo.app.theme.router.ModelConst;

class IndexSeoProcessorTest {
    private final SystemConfigFetcher settings = mock(SystemConfigFetcher.class);
    private final ITemplateContext context = mock(ITemplateContext.class);
    private final IModel model = mock(IModel.class);
    private final IModelFactory factory = mock(IModelFactory.class);

    @Test
    void usesLocalizedSiteSeoForIndexMetadata() {
        when(context.getVariable(ModelConst.TEMPLATE_ID)).thenReturn("index");
        when(context.getModelFactory()).thenReturn(factory);
        when(context.getVariable("site"))
                .thenReturn(SiteSettingVo.builder()
                        .seo(new SiteSettingVo.SeoSetting(false, "translated keywords", "Translated description"))
                        .build());
        new IndexSeoProcessor(settings)
                .process(context, model, null)
                .as(StepVerifier::create)
                .verifyComplete();
        verifyMeta("keywords", "translated keywords");
        verifyMeta("description", "Translated description");
        verifyNoInteractions(settings);
    }

    @Test
    void retainsOriginalSeoWhenSiteVariableIsAbsent() {
        when(context.getVariable(ModelConst.TEMPLATE_ID)).thenReturn("index");
        when(context.getModelFactory()).thenReturn(factory);
        var seo = new SystemSetting.Seo();
        seo.setDescription("Original description");
        when(settings.fetch(SystemSetting.Seo.GROUP, SystemSetting.Seo.class)).thenReturn(Mono.just(seo));
        new IndexSeoProcessor(settings)
                .process(context, model, null)
                .as(StepVerifier::create)
                .verifyComplete();
        verifyMeta("description", "Original description");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void serializesSeoValuesAsAttributeText(boolean localized) throws IOException {
        String text = "你好 \"quoted\" & <tag> 'text' &amp; \"><script>window.seoInjected = true</script>";
        var engine = new TemplateEngine();
        engine.setDialects(Set.of());
        var actualFactory = engine.getConfiguration().getModelFactory(TemplateMode.HTML);
        var actualModel = actualFactory.createModel();
        when(context.getVariable(ModelConst.TEMPLATE_ID)).thenReturn("index");
        when(context.getModelFactory()).thenReturn(actualFactory);
        var seo = new SystemSetting.Seo();
        seo.setDescription(text);
        seo.setKeywords(text);
        var site = SiteSettingVo.builder()
                .seo(new SiteSettingVo.SeoSetting(false, text, text))
                .build();
        if (localized) {
            when(context.getVariable("site")).thenReturn(site);
        } else {
            when(settings.fetch(SystemSetting.Seo.GROUP, SystemSetting.Seo.class))
                    .thenReturn(Mono.just(seo));
        }

        new IndexSeoProcessor(settings)
                .process(context, actualModel, null)
                .as(StepVerifier::create)
                .verifyComplete();

        var output = new StringWriter();
        actualModel.write(output);
        var document = Jsoup.parse(output.toString());
        assertThat(document.select("meta")).hasSize(2);
        assertThat(document.select("meta[name=description]").attr("content")).isEqualTo(text);
        assertThat(document.select("meta[name=keywords]").attr("content")).isEqualTo(text);
        assertThat(document.select("script, tag")).isEmpty();
        assertThat(output.toString()).contains("&quot;", "&amp;", "&lt;", "&gt;");
        assertThat(seo.getDescription()).isEqualTo(text);
        assertThat(seo.getKeywords()).isEqualTo(text);
        assertThat(site.seo().description()).isEqualTo(text);
        assertThat(site.seo().keywords()).isEqualTo(text);
    }

    private void verifyMeta(String name, String value) {
        verify(factory)
                .createStandaloneElementTag(
                        eq("meta"),
                        eq(Map.of("name", name, "content", value)),
                        eq(AttributeValueQuotes.DOUBLE),
                        eq(false),
                        eq(true));
    }
}
