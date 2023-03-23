package run.halo.app.theme.dialect;

import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.spring6.context.SpringContextUtils;
import org.thymeleaf.templatemode.TemplateMode;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;

/**
 * <p>Footer element tag processor.</p>
 * <p>Replace the footer tag <code>&#x3C;halo:footer /&#x3E;</code> with the contents of the footer
 * field of the global configuration item.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class TemplateFooterElementTagProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "footer";
    private static final int PRECEDENCE = 1000;

    /**
     * Constructor footer element tag processor with HTML mode, dialect prefix, footer tag name.
     *
     * @param dialectPrefix dialect prefix
     */
    public TemplateFooterElementTagProcessor(final String dialectPrefix) {
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
        /*
         * Obtain the Spring application context.
         */
        final ApplicationContext appCtx = SpringContextUtils.getApplicationContext(context);
        String globalFooterText = getGlobalFooterText(appCtx);
        structureHandler.replaceWith(globalFooterText, false);
    }

    private String getGlobalFooterText(ApplicationContext appCtx) {
        SystemConfigurableEnvironmentFetcher fetcher =
            appCtx.getBean(SystemConfigurableEnvironmentFetcher.class);
        return fetcher.fetch(SystemSetting.CodeInjection.GROUP, SystemSetting.CodeInjection.class)
            .map(SystemSetting.CodeInjection::getFooter)
            .block();
    }
}
