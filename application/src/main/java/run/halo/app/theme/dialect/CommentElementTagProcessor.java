package run.halo.app.theme.dialect;

import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.util.Optional;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;
import org.thymeleaf.context.Contexts;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.spring6.context.SpringContextUtils;
import org.thymeleaf.templatemode.TemplateMode;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;


/**
 * <p>Comment element tag processor.</p>
 * <p>Replace the comment tag <code>&#x3C;halo:comment /&#x3E;</code> with the given content.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class CommentElementTagProcessor extends AbstractElementTagProcessor {

    public static final String COMMENT_ENABLED_MODEL_ATTRIBUTE = "haloCommentEnabled";
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
        getCommentWidget(context).ifPresentOrElse(commentWidget -> {
            populateAllowCommentAttribute(context, true);
            commentWidget.render(context, tag, structureHandler);
        }, () -> {
            populateAllowCommentAttribute(context, false);
            structureHandler.replaceWith("", false);
        });
    }

    static void populateAllowCommentAttribute(ITemplateContext context, boolean allowComment) {
        if (Contexts.isWebContext(context)) {
            IWebContext webContext = Contexts.asWebContext(context);
            webContext.getExchange()
                .setAttributeValue(COMMENT_ENABLED_MODEL_ATTRIBUTE, allowComment);
        }
    }

    static Optional<CommentWidget> getCommentWidget(ITemplateContext context) {
        final ApplicationContext appCtx = SpringContextUtils.getApplicationContext(context);
        SystemConfigurableEnvironmentFetcher environmentFetcher =
            appCtx.getBean(SystemConfigurableEnvironmentFetcher.class);
        var commentSetting = environmentFetcher.fetchComment()
            .blockOptional()
            .orElseThrow();
        var globalEnabled = isTrue(commentSetting.getEnable());
        if (!globalEnabled) {
            return Optional.empty();
        }

        if (Contexts.isWebContext(context)) {
            IWebContext webContext = Contexts.asWebContext(context);
            Object attributeValue = webContext.getExchange()
                .getAttributeValue(CommentWidget.ENABLE_COMMENT_ATTRIBUTE);
            Boolean enabled = DefaultConversionService.getSharedInstance()
                .convert(attributeValue, Boolean.class);
            if (isFalse(enabled)) {
                return Optional.empty();
            }
        }

        ExtensionGetter extensionGetter = appCtx.getBean(ExtensionGetter.class);
        return extensionGetter.getEnabledExtensionByDefinition(CommentWidget.class)
            .next()
            .blockOptional();
    }
}
