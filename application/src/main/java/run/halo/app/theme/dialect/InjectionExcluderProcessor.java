package run.halo.app.theme.dialect;

import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.util.Assert;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.ITemplateEnd;
import org.thymeleaf.model.ITemplateStart;
import org.thymeleaf.processor.templateboundaries.AbstractTemplateBoundariesProcessor;
import org.thymeleaf.processor.templateboundaries.ITemplateBoundariesProcessor;
import org.thymeleaf.processor.templateboundaries.ITemplateBoundariesStructureHandler;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * <p>Determine whether the current template being rendered needs to exclude the processor of
 * code injection. If it needs to be excluded, set a local variable.</p>
 * <p>Why do you need to set a local variable here instead of directly judging in the processor?</p>
 * <p>Because the processor will process the fragment, and if you need to exclude the <code>login
 * .html</code> template and the login.html is only a fragment, then the exclusion logic will
 * fail, so here use {@link ITemplateBoundariesProcessor} events are only fired for the
 * first-level template to solve this problem.</p>
 *
 * @author guqing
 * @since 2.20.0
 */
public class InjectionExcluderProcessor extends AbstractTemplateBoundariesProcessor {

    public static final String EXCLUDE_INJECTION_VARIABLE =
        InjectionExcluderProcessor.class.getName() + ".EXCLUDE_INJECTION";

    private final PageInjectionExcluder injectionExcluder = new PageInjectionExcluder();

    public InjectionExcluderProcessor() {
        super(TemplateMode.HTML, StandardDialect.PROCESSOR_PRECEDENCE);
    }

    @Override
    public void doProcessTemplateStart(ITemplateContext context, ITemplateStart templateStart,
        ITemplateBoundariesStructureHandler structureHandler) {
        if (isExcluded(context)) {
            structureHandler.setLocalVariable(EXCLUDE_INJECTION_VARIABLE, true);
        }
    }

    @Override
    public void doProcessTemplateEnd(ITemplateContext context, ITemplateEnd templateEnd,
        ITemplateBoundariesStructureHandler structureHandler) {
        structureHandler.removeLocalVariable(EXCLUDE_INJECTION_VARIABLE);
    }

    /**
     * Check if the template will be rendered is excluded injection.
     *
     * @param context template context
     * @return true if the template is excluded, otherwise false
     */
    boolean isExcluded(ITemplateContext context) {
        return injectionExcluder.isExcluded(context.getTemplateData().getTemplate());
    }

    static class PageInjectionExcluder {

        private final Set<String> exactMatches = Set.of(
            "login",
            "signup",
            "logout"
        );

        private final Set<Pattern> regexPatterns = Set.of(
            Pattern.compile("error/.*"),
            Pattern.compile("challenges/.*"),
            Pattern.compile("password-reset/.*")
        );

        public boolean isExcluded(String templateName) {
            Assert.notNull(templateName, "Template name must not be null");
            if (exactMatches.contains(templateName)) {
                return true;
            }

            for (Pattern pattern : regexPatterns) {
                if (pattern.matcher(templateName).matches()) {
                    return true;
                }
            }

            return false;
        }
    }
}
