package run.halo.app.theme.dialect;

import java.util.LinkedHashMap;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.AttributeValueQuotes;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.vo.SiteSettingVo;
import run.halo.app.theme.router.ModelConst;

/**
 * Processor for index page SEO.
 *
 * @author ryanwang
 */
@Component
@AllArgsConstructor
class IndexSeoProcessor implements TemplateHeadProcessor {

    private final SystemConfigFetcher environmentFetcher;

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler) {
        if (!isIndexTemplate(context)) {
            return Mono.empty();
        }
        return seoSettings(context)
                .doOnNext(seo -> {
                    var factory = context.getModelFactory();
                    addMeta(model, factory, "keywords", seo.keywords());
                    addMeta(model, factory, "description", seo.description());
                })
                .then();
    }

    private Mono<SiteSettingVo.SeoSetting> seoSettings(ITemplateContext context) {
        if (context.getVariable("site") instanceof SiteSettingVo site) {
            return Mono.justOrEmpty(site.seo());
        }
        return environmentFetcher
                .fetch(SystemSetting.Seo.GROUP, SystemSetting.Seo.class)
                .map(seo ->
                        new SiteSettingVo.SeoSetting(seo.getBlockSpiders(), seo.getKeywords(), seo.getDescription()));
    }

    private void addMeta(IModel model, IModelFactory factory, String name, String content) {
        if (StringUtils.isBlank(content)) {
            return;
        }
        var attributes = new LinkedHashMap<String, String>();
        attributes.put("name", name);
        attributes.put("content", HtmlUtils.htmlEscape(content));
        model.add(factory.createStandaloneElementTag("meta", attributes, AttributeValueQuotes.DOUBLE, false, true));
        model.add(factory.createText("\n"));
    }

    private boolean isIndexTemplate(ITemplateContext context) {
        return DefaultTemplateEnum.INDEX.getValue().equals(context.getVariable(ModelConst.TEMPLATE_ID));
    }
}
