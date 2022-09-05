package run.halo.app.theme.dialect;

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

/**
 * <p>Global custom head snippet injection for theme global setting.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class TemplateGlobalHeadProcessor implements TemplateHeadProcessor {

    private final SystemConfigurableEnvironmentFetcher fetcher;

    public TemplateGlobalHeadProcessor(SystemConfigurableEnvironmentFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        final IModelFactory modelFactory = context.getModelFactory();
        return fetchCodeInjection()
            .doOnNext(codeInjection -> {
                String globalHeader = codeInjection.getGlobalHead();
                if (StringUtils.isNotBlank(globalHeader)) {
                    model.add(modelFactory.createText(globalHeader + "\n"));
                }

                // add content head to model
                String contentHeader = codeInjection.getContentHead();
                String template = context.getTemplateData().getTemplate();
                if (StringUtils.isNotBlank(contentHeader) && isContentTemplate(template)) {
                    model.add(modelFactory.createText(contentHeader + "\n"));
                }
            })
            .then();
    }

    private Mono<SystemSetting.CodeInjection> fetchCodeInjection() {
        return fetcher.fetch(SystemSetting.CodeInjection.GROUP, SystemSetting.CodeInjection.class);
    }

    private boolean isContentTemplate(String template) {
        // TODO includes custom page template
        return DefaultTemplateEnum.POST.getValue().equals(template);
    }
}
