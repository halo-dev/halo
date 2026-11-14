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
        return environmentFetcher
                .fetch(SystemSetting.Seo.GROUP, SystemSetting.Seo.class)
                .doOnNext(seo -> {
                    IModelFactory modelFactory = context.getModelFactory();

                    String keywords = seo.getKeywords();
                    if (StringUtils.isNotBlank(keywords)) {
                        var attributes = LinkedHashMap.<String, String>newLinkedHashMap(2);
                        attributes.put("name", "keywords");
                        attributes.put("content", HtmlUtils.htmlEscape(keywords));
                        model.add(modelFactory.createStandaloneElementTag(
                                "meta", attributes, AttributeValueQuotes.DOUBLE, false, true));
                        model.add(modelFactory.createText("\n"));
                    }

                    if (StringUtils.isNotBlank(seo.getDescription())) {
                        var attributes = LinkedHashMap.<String, String>newLinkedHashMap(2);
                        attributes.put("name", "description");
                        attributes.put("content", HtmlUtils.htmlEscape(seo.getDescription()));
                        model.add(modelFactory.createStandaloneElementTag(
                                "meta", attributes, AttributeValueQuotes.DOUBLE, false, true));
                        model.add(modelFactory.createText("\n"));
                    }
                })
                .then();
    }

    private boolean isIndexTemplate(ITemplateContext context) {
        return DefaultTemplateEnum.INDEX.getValue().equals(context.getVariable(ModelConst.TEMPLATE_ID));
    }
}
