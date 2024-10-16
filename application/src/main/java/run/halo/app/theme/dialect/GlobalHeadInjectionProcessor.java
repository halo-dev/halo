package run.halo.app.theme.dialect;

import static org.thymeleaf.spring6.context.SpringContextUtils.getApplicationContext;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;
import reactor.core.publisher.Flux;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

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
        if (context.containsVariable(InjectionExcluderProcessor.EXCLUDE_INJECTION_VARIABLE)) {
            return;
        }

        // note that this is important!!
        Object processedAlready = context.getVariable(PROCESS_FLAG);
        if (processedAlready != null) {
            return;
        }
        structureHandler.setLocalVariable(PROCESS_FLAG, true);

        // handle <head> tag
        if (model.size() < 2) {
            return;
        }

        /*
         * Create the DOM structure that will be substituting our custom tag.
         * The headline will be shown inside a '<div>' tag, and so this must
         * be created first and then a Text node must be added to it.
         */
        IModel modelToInsert = model.cloneModel();
        // close </head> tag
        final ITemplateEvent closeHeadTag = modelToInsert.get(modelToInsert.size() - 1);
        modelToInsert.remove(modelToInsert.size() - 1);

        // open <head> tag
        final ITemplateEvent openHeadTag = modelToInsert.get(0);
        modelToInsert.remove(0);

        // apply processors to modelToInsert
        getTemplateHeadProcessors(context)
            .concatMap(processor -> processor.process(
                SecureTemplateContextWrapper.wrap(context), modelToInsert, structureHandler)
            )
            .then()
            .block();

        // reset model to insert
        model.reset();
        model.add(openHeadTag);
        model.addModel(modelToInsert);
        model.add(closeHeadTag);
    }

    private Flux<TemplateHeadProcessor> getTemplateHeadProcessors(ITemplateContext context) {
        var extensionGetter = getApplicationContext(context).getBeanProvider(ExtensionGetter.class)
            .getIfUnique();
        if (extensionGetter == null) {
            return Flux.empty();
        }
        return extensionGetter.getExtensions(TemplateHeadProcessor.class);
    }
}
