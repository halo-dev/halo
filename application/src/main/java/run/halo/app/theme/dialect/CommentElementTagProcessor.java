package run.halo.app.theme.dialect;

import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.spring6.context.SpringContextUtils;
import org.thymeleaf.templatemode.TemplateMode;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;


/**
 * <p>Comment element tag processor.</p>
 * <p>Replace the comment tag <code>&#x3C;halo:comment /&#x3E;</code> with the given content.</p>
 *
 * @author guqing
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
        final ApplicationContext appCtx = SpringContextUtils.getApplicationContext(context);
        ExtensionGetter extensionGetter = appCtx.getBean(ExtensionGetter.class);
        CommentWidget commentWidget =
            extensionGetter.getEnabledExtensionByDefinition(CommentWidget.class)
                .next()
                .block();
        if (commentWidget == null) {
            structureHandler.replaceWith("", false);
            return;
        }
        commentWidget.render(context, tag, structureHandler);
    }
}
