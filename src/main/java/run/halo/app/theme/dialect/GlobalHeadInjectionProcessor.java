package run.halo.app.theme.dialect;

import java.util.Collection;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.spring6.context.SpringContextUtils;
import org.thymeleaf.templatemode.TemplateMode;
import run.halo.app.plugin.ExtensionComponentsFinder;

/**
 * Global head injection processor.
 *
 * @author guqing
 * @since 2.0.0
 */
public class GlobalHeadInjectionProcessor extends AbstractElementModelProcessor {
    /**
     * Inserting tag will re-trigger this processor, in order to avoid the loop out trigger,
     * this flag is required to prevent the loop problem.
     */
    private static final String PROCESS_FLAG =
        GlobalHeadInjectionProcessor.class.getName() + ".PROCESSED";

    private static final String TAG_NAME = "head";
    private static final int PRECEDENCE = 1000;

    public GlobalHeadInjectionProcessor(final String dialectPrefix) {
        super(
            TemplateMode.HTML, // This processor will apply only to HTML mode
            dialectPrefix,     // Prefix to be applied to name for matching
            TAG_NAME,          // Tag name: match specifically this tag
            false,              // Apply dialect prefix to tag name
            null,              // No attribute name: will match by tag name
            false,             // No prefix to be applied to attribute name
            PRECEDENCE);       // Precedence (inside dialect's own precedence)
    }

    @Override
    protected void doProcess(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {

        // note that this is important!!
        Object processedAlready = context.getVariable(PROCESS_FLAG);
        if (processedAlready != null) {
            return;
        }
        structureHandler.setLocalVariable(PROCESS_FLAG, true);

        // handle <head> tag

        /*
         * Create the DOM structure that will be substituting our custom tag.
         * The headline will be shown inside a '<div>' tag, and so this must
         * be created first and then a Text node must be added to it.
         */
        final IModelFactory modelFactory = context.getModelFactory();
        IModel modelToInsert = modelFactory.createModel();

        // apply processors to modelToInsert
        Collection<TemplateHeadProcessor> templateHeadProcessors =
            getTemplateHeadProcessors(context);

        for (TemplateHeadProcessor processor : templateHeadProcessors) {
            processor.process(context, modelToInsert, structureHandler)
                .block();
        }

        // add to target model
        model.insertModel(model.size() - 1, modelToInsert);
    }

    private Collection<TemplateHeadProcessor> getTemplateHeadProcessors(ITemplateContext context) {
        ApplicationContext appCtx = SpringContextUtils.getApplicationContext(context);
        ExtensionComponentsFinder componentsFinder =
            appCtx.getBean(ExtensionComponentsFinder.class);
        return componentsFinder.getExtensions(TemplateHeadProcessor.class);
    }
}
