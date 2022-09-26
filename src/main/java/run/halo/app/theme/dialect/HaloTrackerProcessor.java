package run.halo.app.theme.dialect;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.TemplateData;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
public class HaloTrackerProcessor implements TemplateHeadProcessor {

    private final HaloProperties haloProperties;

    public HaloTrackerProcessor(HaloProperties haloProperties) {
        this.haloProperties = haloProperties;
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        final IModelFactory modelFactory = context.getModelFactory();
        return Mono.just(getTrackerScript(context))
            .filter(StringUtils::isNotBlank)
            .map(trackerScript -> {
                model.add(modelFactory.createText(trackerScript));
                return trackerScript;
            })
            .then();
    }

    private String getTrackerScript(ITemplateContext context) {
        TemplateData templateData = context.getTemplateData();
        String templateName = templateData.getTemplate();
        String resourceName = (String) context.getVariable("name");
        String externalUrl = haloProperties.getExternalUrl().getPath();
        DefaultTemplateEnum templateEnum = DefaultTemplateEnum.convertFrom(templateName);
        if (templateEnum == null) {
            return StringUtils.EMPTY;
        }
        String scriptString;
        switch (templateEnum) {
            case POST -> {
                GVK gvk = getGvk(Post.class);
                scriptString = trackerScript(externalUrl, gvk.group(), gvk.plural(), resourceName);
            }
            case SINGLE_PAGE -> {
                GVK gvk = getGvk(SinglePage.class);
                scriptString = trackerScript(externalUrl, gvk.group(), gvk.plural(), resourceName);
            }
            default -> scriptString = StringUtils.EMPTY;
        }
        return scriptString;
    }

    @NonNull
    private <T extends AbstractExtension> GVK getGvk(Class<T> extensionClass) {
        return extensionClass.getAnnotation(GVK.class);
    }

    private String trackerScript(String externalUrl, String group, String plural, String name) {
        String jsSrc = PathUtils.combinePath(externalUrl, "/halo-tracker.js");
        return """
            <script async defer src="%s" data-group="%s" data-plural="%s" data-name="%s"></script>
            """.formatted(jsSrc, group, plural, name);
    }
}
