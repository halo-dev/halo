package run.halo.app.theme.dialect;

import org.springframework.integration.json.JsonPropertyAccessor;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.ITemplateEnd;
import org.thymeleaf.model.ITemplateStart;
import org.thymeleaf.processor.templateboundaries.AbstractTemplateBoundariesProcessor;
import org.thymeleaf.processor.templateboundaries.ITemplateBoundariesStructureHandler;
import org.thymeleaf.spring6.expression.ThymeleafEvaluationContext;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templatemode.TemplateMode;
import run.halo.app.theme.ReactivePropertyAccessor;

/**
 * A template boundaries processor for add {@link JsonPropertyAccessor} to
 * {@link ThymeleafEvaluationContext}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class JsonNodePropertyAccessorBoundariesProcessor
    extends AbstractTemplateBoundariesProcessor {
    private static final int PRECEDENCE = StandardDialect.PROCESSOR_PRECEDENCE;
    private static final JsonPropertyAccessor JSON_PROPERTY_ACCESSOR = new JsonPropertyAccessor();
    private static final ReactivePropertyAccessor REACTIVE_PROPERTY_ACCESSOR =
        new ReactivePropertyAccessor();

    public JsonNodePropertyAccessorBoundariesProcessor() {
        super(TemplateMode.HTML, PRECEDENCE);
    }

    @Override
    public void doProcessTemplateStart(ITemplateContext context, ITemplateStart templateStart,
        ITemplateBoundariesStructureHandler structureHandler) {
        ThymeleafEvaluationContext evaluationContext =
            (ThymeleafEvaluationContext) context.getVariable(
                ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME);
        if (evaluationContext != null) {
            evaluationContext.addPropertyAccessor(JSON_PROPERTY_ACCESSOR);
            evaluationContext.addPropertyAccessor(REACTIVE_PROPERTY_ACCESSOR);
        }
    }

    @Override
    public void doProcessTemplateEnd(ITemplateContext context, ITemplateEnd templateEnd,
        ITemplateBoundariesStructureHandler structureHandler) {
        // nothing to do
    }
}
