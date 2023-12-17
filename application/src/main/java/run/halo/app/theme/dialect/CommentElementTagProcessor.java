package run.halo.app.theme.dialect;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * <p>Comment element tag processor.</p>
 * <p>Replace the comment tag <code>&#x3C;halo:comment /&#x3E;</code> with the given content.</p>
 *
 * @author guqing
 * @see CommentEnabledVariableProcessor
 * @since 2.0.0
 */
public class CommentElementTagProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "comment";

    private static final int PRECEDENCE = 1000;

    /**
     * Constructor footer element tag processor with HTML mode, dialect prefix, comment tag name.
     *
     * @param dialectPrefix dialect prefix
     */
    public CommentElementTagProcessor(final String dialectPrefix) {
        super(
            TemplateMode.HTML, // This processor will apply only to HTML mode
            dialectPrefix,     // Prefix to be applied to name for matching
            TAG_NAME,          // Tag name: match specifically this tag
            true,              // Apply dialect prefix to tag name
            null,              // No attribute name: will match by tag name
            false,             // No prefix to be applied to attribute name
            PRECEDENCE);       // Precedence (inside dialect's own precedence)
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
        IElementTagStructureHandler structureHandler) {
        var commentWidget = (CommentWidget) context.getVariable(
            CommentEnabledVariableProcessor.COMMENT_WIDGET_OBJECT_VARIABLE);
        if (commentWidget == null) {
            structureHandler.replaceWith("", false);
            return;
        }
        commentWidget.render(context, tag, structureHandler);
    }
}
