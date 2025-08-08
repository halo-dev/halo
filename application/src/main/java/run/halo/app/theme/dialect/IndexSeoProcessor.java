package run.halo.app.theme.dialect;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
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

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        if (!isIndexTemplate(context)) {
            return Mono.empty();
        }
        return environmentFetcher.fetch(SystemSetting.Seo.GROUP, SystemSetting.Seo.class)
            .map(seo -> {
                IModelFactory modelFactory = context.getModelFactory();

                String keywords = seo.getKeywords();
                if (StringUtils.isNotBlank(keywords)) {
                    String keywordsMeta =
                        "<meta name=\"keywords\" content=\"" + keywords + "\" />\n";
                    model.add(modelFactory.createText(keywordsMeta));
                }

                if (StringUtils.isNotBlank(seo.getDescription())) {
                    String descriptionMeta =
                        "<meta name=\"description\" content=\"" + seo.getDescription() + "\" />\n";
                    model.add(modelFactory.createText(descriptionMeta));
                }
                return model;
            })
            .then();
    }

    private boolean isIndexTemplate(ITemplateContext context) {
        return DefaultTemplateEnum.INDEX.getValue()
            .equals(context.getVariable(ModelConst.TEMPLATE_ID));
    }
}
