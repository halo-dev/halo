package run.halo.app.theme.dialect;

import static run.halo.app.theme.Constant.META_DESCRIPTION_VARIABLE_NAME;

import java.util.Map;
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
                    var metaTag = modelFactory.createStandaloneElementTag(
                        "meta",
                        Map.of(
                            "name", "robots",
                            "content", "noindex"
                        ),
                        AttributeValueQuotes.DOUBLE,
                        false,
                        true
                    );
                    model.add(metaTag);
                    return;
                }
                var seoMetaDescription = context.getVariable(META_DESCRIPTION_VARIABLE_NAME);
                if (seoMetaDescription instanceof String description && !description.isBlank()) {
                    var metaTag = modelFactory.createStandaloneElementTag(
                        "meta",
                        Map.of(
                            "name", "description",
                            "content", description
                        ),
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
