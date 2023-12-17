package run.halo.app.theme.dialect;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.utils.PathUtils;

/**
 * Get {@link GroupVersionKind} and {@code plural} from the view model to construct tracker
 * script tag and insert it into the head tag.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class HaloTrackerProcessor implements TemplateHeadProcessor {

    private final ExternalUrlSupplier externalUrlGetter;

    public HaloTrackerProcessor(ExternalUrlSupplier externalUrlGetter) {
        this.externalUrlGetter = externalUrlGetter;
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
        String resourceName = (String) context.getVariable("name");
        String externalUrl = externalUrlGetter.get().getPath();
        Object groupVersionKind = context.getVariable("groupVersionKind");
        Object plural = context.getVariable("plural");
        if (groupVersionKind == null || plural == null) {
            return StringUtils.EMPTY;
        }
        if (!(groupVersionKind instanceof GroupVersionKind gvk)) {
            return StringUtils.EMPTY;
        }
        return trackerScript(externalUrl, gvk.group(), (String) plural, resourceName);
    }

    private String trackerScript(String externalUrl, String group, String plural, String name) {
        String jsSrc = PathUtils.combinePath(externalUrl, "/halo-tracker.js");
        return """
            <script async defer src="%s" data-group="%s" data-plural="%s" data-name="%s"></script>
            """.formatted(jsSrc, group, plural, name);
    }
}
