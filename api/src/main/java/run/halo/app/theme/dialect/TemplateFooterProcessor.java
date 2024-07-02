package run.halo.app.theme.dialect;

import org.pf4j.ExtensionPoint;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import reactor.core.publisher.Mono;

/**
 * Theme template <code>footer</code> tag snippet injection processor.
 *
 * @author guqing
 * @since 2.17.0
 */
public interface TemplateFooterProcessor extends ExtensionPoint {

    Mono<Void> process(ITemplateContext context, IProcessableElementTag tag,
        IElementTagStructureHandler structureHandler, IModel model);
}
