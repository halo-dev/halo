package run.halo.app.theme.dialect;

import static org.thymeleaf.spring6.context.SpringContextUtils.getApplicationContext;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AbstractTemplateHandler;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.IStandaloneElementTag;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/**
 * Template post-handler.
 *
 * @author johnniang
 * @since 2.20.0
 */
public class HaloPostTemplateHandler extends AbstractTemplateHandler {

    private List<ElementTagPostProcessor> postProcessors = List.of();

    @Override
    public void setContext(ITemplateContext context) {
        super.setContext(context);
        this.postProcessors = Optional.ofNullable(getApplicationContext(context))
            .map(appContext -> appContext.getBeanProvider(ExtensionGetter.class).getIfUnique())
            .map(extensionGetter -> extensionGetter.getExtensionList(ElementTagPostProcessor.class))
            .orElseGet(List::of);
    }

    @Override
    public void handleStandaloneElement(IStandaloneElementTag standaloneElementTag) {
        var processedTag = handleElementTag(standaloneElementTag);
        super.handleStandaloneElement((IStandaloneElementTag) processedTag);
    }

    @Override
    public void handleOpenElement(IOpenElementTag openElementTag) {
        var processedTag = handleElementTag(openElementTag);
        super.handleOpenElement((IOpenElementTag) processedTag);
    }

    @NonNull
    private IProcessableElementTag handleElementTag(
        @NonNull IProcessableElementTag processableElementTag
    ) {
        IProcessableElementTag processedTag = processableElementTag;
        if (!CollectionUtils.isEmpty(postProcessors)) {
            var tagProcessorChain = Mono.just(processableElementTag);
            var context = getContext();
            for (ElementTagPostProcessor elementTagPostProcessor : postProcessors) {
                tagProcessorChain = tagProcessorChain.flatMap(
                    tag -> elementTagPostProcessor.process(
                            SecureTemplateContextWrapper.wrap(context), tag)
                        .defaultIfEmpty(tag)
                );
            }
            processedTag =
                Objects.requireNonNull(tagProcessorChain.defaultIfEmpty(processableElementTag)
                    .block(Duration.ofMinutes(1)));
        }
        return processedTag;
    }
}
