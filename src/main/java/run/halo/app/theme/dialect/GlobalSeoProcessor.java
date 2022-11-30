package run.halo.app.theme.dialect;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;

/**
 * Inject code to the template head tag according to the global seo settings.
 *
 * @author guqing
 * @see SystemSetting.Seo
 * @since 2.0.0
 */
@Order
@Component
@AllArgsConstructor
public class GlobalSeoProcessor implements TemplateHeadProcessor {

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        return environmentFetcher.fetch(SystemSetting.Seo.GROUP, SystemSetting.Seo.class)
            .map(seo -> {
                boolean blockSpiders = BooleanUtils.isTrue(seo.getBlockSpiders());
                IModelFactory modelFactory = context.getModelFactory();
                if (blockSpiders) {
                    String noIndexMeta = "<meta name=\"robots\" content=\"noindex\" />\n";
                    model.add(modelFactory.createText(noIndexMeta));
                    return model;
                }

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
}
