package run.halo.app.theme.dialect;

import static org.thymeleaf.model.AttributeValueQuotes.DOUBLE;

import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.annotation.Order;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;

/**
 * Processor for generating generator meta.
 * Set the order to 0 for removing the meta in later TemplateHeadProcessor.
 *
 * @author johnniang
 */
@Order(0)
public class GeneratorMetaProcessor implements TemplateHeadProcessor {

    private final String generatorValue;

    public GeneratorMetaProcessor(ObjectProvider<BuildProperties> buildProperties) {
        this.generatorValue = "Halo " + buildProperties.stream().findFirst()
            .map(BuildProperties::getVersion)
            .orElse("Unknown");
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        return Mono.fromRunnable(() -> {
            var modelFactory = context.getModelFactory();
            var generatorMeta = modelFactory.createStandaloneElementTag("meta",
                Map.of("name", "generator", "content", generatorValue),
                DOUBLE, false, true);
            model.add(generatorMeta);
        });
    }

}
