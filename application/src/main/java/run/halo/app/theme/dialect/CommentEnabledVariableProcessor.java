package run.halo.app.theme.dialect;

import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.util.Optional;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;
import org.thymeleaf.context.Contexts;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.model.ITemplateEnd;
import org.thymeleaf.model.ITemplateStart;
import org.thymeleaf.processor.templateboundaries.AbstractTemplateBoundariesProcessor;
import org.thymeleaf.processor.templateboundaries.ITemplateBoundariesStructureHandler;
import org.thymeleaf.spring6.context.SpringContextUtils;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templatemode.TemplateMode;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/**
 * Comment enabled variable processor.
 * <p>Compute comment enabled state and set it to the model when the template is start rendering</p>
 * <p>It is not suitable for scenarios where there are multiple comment components on the same page
 * and some of them need to be controlled to be closed.</p>
 *
 * @author guqing
 * @since 2.9.0
 */
public class CommentEnabledVariableProcessor extends AbstractTemplateBoundariesProcessor {

    public static final String COMMENT_WIDGET_OBJECT_VARIABLE = CommentWidget.class.getName();
    public static final String COMMENT_ENABLED_MODEL_ATTRIBUTE = "haloCommentEnabled";

    public CommentEnabledVariableProcessor() {
        super(TemplateMode.HTML, StandardDialect.PROCESSOR_PRECEDENCE);
    }

    @Override
    public void doProcessTemplateStart(ITemplateContext context, ITemplateStart templateStart,
        ITemplateBoundariesStructureHandler structureHandler) {
        getCommentWidget(context).ifPresentOrElse(commentWidget -> {
            populateAllowCommentAttribute(context, true);
            structureHandler.setLocalVariable(COMMENT_WIDGET_OBJECT_VARIABLE, commentWidget);
        }, () -> populateAllowCommentAttribute(context, false));
    }

    @Override
    public void doProcessTemplateEnd(ITemplateContext context, ITemplateEnd templateEnd,
        ITemplateBoundariesStructureHandler structureHandler) {
        structureHandler.removeLocalVariable(COMMENT_WIDGET_OBJECT_VARIABLE);
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
