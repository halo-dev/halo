package run.halo.app.theme.dialect;

import org.pf4j.ExtensionPoint;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;

/**
 * Comment widget extension point to extend the &#x3C;halo:comment /&#x3E; tag of the theme-side.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface CommentWidget extends ExtensionPoint {

    void render(ITemplateContext context, IProcessableElementTag tag,
        IElementTagStructureHandler structureHandler);
}
