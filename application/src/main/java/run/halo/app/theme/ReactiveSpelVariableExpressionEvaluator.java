package run.halo.app.theme;

import java.util.Optional;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.spring6.expression.SPELVariableExpressionEvaluator;
import org.thymeleaf.standard.expression.IStandardVariableExpression;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
import org.thymeleaf.standard.expression.StandardExpressionExecutionContext;
import run.halo.app.infra.utils.ReactiveUtils;

/**
 * Reactive SPEL variable expression evaluator.
 *
 * @author guqing
 * @since 2.0.0
 */
public class ReactiveSpelVariableExpressionEvaluator
    implements IStandardVariableExpressionEvaluator {

    private final IStandardVariableExpressionEvaluator delegate;

    public static final ReactiveSpelVariableExpressionEvaluator INSTANCE =
        new ReactiveSpelVariableExpressionEvaluator();

    public ReactiveSpelVariableExpressionEvaluator(IStandardVariableExpressionEvaluator delegate) {
        this.delegate = delegate;
    }

    public ReactiveSpelVariableExpressionEvaluator() {
        this(SPELVariableExpressionEvaluator.INSTANCE);
    }

    @Override
    public Object evaluate(IExpressionContext context, IStandardVariableExpression expression,
        StandardExpressionExecutionContext expContext) {
        var returnValue = delegate.evaluate(context, expression, expContext);
        return Optional.ofNullable(returnValue)
            .map(ReactiveUtils::blockReactiveValue)
            .orElse(null);
    }
}
