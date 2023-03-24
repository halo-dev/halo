package run.halo.app.theme.dialect;

import org.pf4j.ExtensionPoint;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;

/**
 * Theme template <code>head</code> tag snippet injection processor.
 *
 * @author guqing
 * @since 2.0.0
 */
@FunctionalInterface
public interface TemplateHeadProcessor extends ExtensionPoint {

    Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler);
}
