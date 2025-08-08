package run.halo.app.theme.dialect;

import static run.halo.app.theme.Constant.META_DESCRIPTION_VARIABLE_NAME;

import java.util.LinkedHashMap;
import lombok.AllArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.AttributeValueQuotes;
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
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Component
@AllArgsConstructor
class GlobalSeoProcessor implements TemplateHeadProcessor {

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        return environmentFetcher.fetch(SystemSetting.Seo.GROUP, SystemSetting.Seo.class)
            .switchIfEmpty(Mono.fromSupplier(SystemSetting.Seo::new))
            .doOnNext(seo -> {
                IModelFactory modelFactory = context.getModelFactory();
                if (Boolean.TRUE.equals(seo.getBlockSpiders())) {
                    var attributes = LinkedHashMap.<String, String>newLinkedHashMap(2);
                    attributes.put("name", "robots");
                    attributes.put("content", "noindex");
                    var metaTag = modelFactory.createStandaloneElementTag(
                        "meta",
                        attributes,
                        AttributeValueQuotes.DOUBLE,
                        false,
                        true
                    );
                    model.add(metaTag);
                    return;
                }
                var seoMetaDescription = context.getVariable(META_DESCRIPTION_VARIABLE_NAME);
                if (seoMetaDescription instanceof String description && !description.isBlank()) {
                    var attributes = LinkedHashMap.<String, String>newLinkedHashMap(2);
                    attributes.put("name", "description");
                    attributes.put("content", description);
                    var metaTag = modelFactory.createStandaloneElementTag(
                        "meta",
                        attributes,
                        AttributeValueQuotes.DOUBLE,
                        false,
                        true
                    );
                    model.add(metaTag);
                }
            })
            .then();
    }
}
