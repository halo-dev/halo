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

/**
 * Theme template <code>head</code> tag snippet injection processor for favicon.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class DefaultFaviconHeadProcessor implements TemplateHeadProcessor {

    private final SystemConfigFetcher fetcher;

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler) {
        return fetchBasicSetting()
                .filter(basic -> StringUtils.isNotBlank(basic.getFavicon()))
                .doOnNext(basic -> {
                    IModelFactory modelFactory = context.getModelFactory();
                    var attributes = LinkedHashMap.<String, String>newLinkedHashMap(2);
                    attributes.put("rel", "icon");
                    attributes.put("href", HtmlUtils.htmlEscape(basic.getFavicon()));
                    var linkTag = modelFactory.createStandaloneElementTag(
                            "link", attributes, AttributeValueQuotes.DOUBLE, false, true);
                    model.add(linkTag);
                    model.add(modelFactory.createText("\n"));
                })
                .then();
    }

    private Mono<SystemSetting.Basic> fetchBasicSetting() {
        return fetcher.fetch(SystemSetting.Basic.GROUP, SystemSetting.Basic.class);
    }
}
