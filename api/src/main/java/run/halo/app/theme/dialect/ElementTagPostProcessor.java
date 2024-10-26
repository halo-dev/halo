package run.halo.app.theme.dialect;

import org.pf4j.ExtensionPoint;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import reactor.core.publisher.Mono;

/**
 * An extension point for post-processing element tag.
 *
 * @author johnniang
 * @since 2.20.0
 */
public interface ElementTagPostProcessor extends ExtensionPoint {

    /**
     * <p>
     * Execute the processor.
     * </p>
     * <p>
     * The {@link IProcessableElementTag} object argument is immutable, so all modifications to
     * this object or any
     * instructions to be given to the engine should be done through the specified
     * {@link org.thymeleaf.model.IModelFactory} model factory in context.
     * </p>
     * <p>
     * Don't forget to return the new tag after processing or
     * {@link reactor.core.publisher.Mono#empty()} if not processable.
     * </p>
     *
     * @param context the template context.
     * @param tag the event this processor is executing on.
     * @return a {@link reactor.core.publisher.Mono} that will complete when processing finishes
     * or empty mono if not support.
     */
    Mono<IProcessableElementTag> process(
        ITemplateContext context,
        final IProcessableElementTag tag
    );

}
