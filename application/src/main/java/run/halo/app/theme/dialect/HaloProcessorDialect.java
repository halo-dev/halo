package run.halo.app.theme.dialect;

import java.util.HashSet;
import java.util.Set;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

/**
 * Thymeleaf processor dialect for Halo.
 *
 * @author guqing
 * @since 2.0.0
 */
public class HaloProcessorDialect extends AbstractProcessorDialect implements
    IExpressionObjectDialect {
    private static final String DIALECT_NAME = "haloThemeProcessorDialect";

    private static final IExpressionObjectFactory HALO_EXPRESSION_OBJECTS_FACTORY =
        new HaloExpressionObjectFactory();

    public HaloProcessorDialect() {
        // We will set this dialect the same "dialect processor" precedence as
        // the Standard Dialect, so that processor executions can interleave.
        super(DIALECT_NAME, "halo", StandardDialect.PROCESSOR_PRECEDENCE);
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        // add more processors
        processors.add(new GlobalHeadInjectionProcessor(dialectPrefix));
        processors.add(new TemplateFooterElementTagProcessor(dialectPrefix));
        processors.add(new JsonNodePropertyAccessorBoundariesProcessor());
        processors.add(new CommentElementTagProcessor(dialectPrefix));
        return processors;
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return HALO_EXPRESSION_OBJECTS_FACTORY;
    }
}
