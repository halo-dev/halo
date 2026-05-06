package run.halo.app.theme.dialect;

import static org.thymeleaf.templatemode.TemplateMode.HTML;

import com.github.zafarkhaja.semver.Version;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.dialect.IPostProcessorDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.postprocessor.PostProcessor;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;
import run.halo.app.infra.SystemVersionSupplier;

/**
 * Thymeleaf processor dialect for Halo.
 *
 * @author guqing
 * @since 2.0.0
 */
public class HaloProcessorDialect extends AbstractProcessorDialect
    implements IExpressionObjectDialect, IPostProcessorDialect {
    private static final String DIALECT_NAME = "haloThemeProcessorDialect";

    private final IExpressionObjectFactory expressionObjectFactory;

    public HaloProcessorDialect() {
        this(null);
    }

    public HaloProcessorDialect(SystemVersionSupplier systemVersionSupplier) {
        // We will set this dialect the same "dialect processor" precedence as
        // the Standard Dialect, so that processor executions can interleave.
        super(DIALECT_NAME, "halo", StandardDialect.PROCESSOR_PRECEDENCE);
        this.expressionObjectFactory = new HaloExpressionObjectFactory(systemVersionSupplier);
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        // add more processors
        processors.add(new GlobalHeadInjectionProcessor(dialectPrefix));
        processors.add(new TemplateFooterElementTagProcessor(dialectPrefix));
        processors.add(new EvaluationContextEnhancer());
        processors.add(new CommentElementTagProcessor(dialectPrefix));
        processors.add(new CommentEnabledVariableProcessor());
        processors.add(new InjectionExcluderProcessor());
        return processors;
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return expressionObjectFactory;
    }

    @Override
    public int getDialectPostProcessorPrecedence() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Set<IPostProcessor> getPostProcessors() {
        return Set.of(new PostProcessor(HTML, HaloPostTemplateHandler.class, Integer.MAX_VALUE));
    }
}
