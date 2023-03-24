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

/**
 * Theme template <code>head</code> tag snippet injection processor for favicon.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class DefaultFaviconHeadProcessor implements TemplateHeadProcessor {

    private final SystemConfigurableEnvironmentFetcher fetcher;

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        return fetchBasicSetting()
            .filter(basic -> StringUtils.isNotBlank(basic.getFavicon()))
            .map(basic -> {
                IModelFactory modelFactory = context.getModelFactory();
                model.add(modelFactory.createText(faviconSnippet(basic.getFavicon())));
                return model;
            })
            .then();
    }

    private String faviconSnippet(String favicon) {
        return String.format("<link rel=\"icon\" href=\"%s\" />\n", favicon);
    }

    private Mono<SystemSetting.Basic> fetchBasicSetting() {
        return fetcher.fetch(SystemSetting.Basic.GROUP, SystemSetting.Basic.class);
    }
}
