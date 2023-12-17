package run.halo.app.theme;

import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.spring6.expression.SPELVariableExpressionEvaluator;
import org.thymeleaf.standard.expression.IStandardVariableExpression;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
import org.thymeleaf.standard.expression.StandardExpressionExecutionContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive SPEL variable expression evaluator.
 *
 * @author guqing
 * @since 2.0.0
 */
public class ReactiveSpelVariableExpressionEvaluator
    implements IStandardVariableExpressionEvaluator {

    private final SPELVariableExpressionEvaluator delegate =
        SPELVariableExpressionEvaluator.INSTANCE;

    public static final ReactiveSpelVariableExpressionEvaluator INSTANCE =
        new ReactiveSpelVariableExpressionEvaluator();

    @Override
    public Object evaluate(IExpressionContext context, IStandardVariableExpression expression,
        StandardExpressionExecutionContext expContext) {
        Object returnValue = delegate.evaluate(context, expression, expContext);
        if (returnValue == null) {
            return null;
        }

        Class<?> clazz = returnValue.getClass();
        // Note that: 3 instanceof Foo -> syntax error
        if (Mono.class.isAssignableFrom(clazz)) {
            return ((Mono<?>) returnValue).block();
        }
        if (Flux.class.isAssignableFrom(clazz)) {
            return ((Flux<?>) returnValue).collectList().block();
        }
        return returnValue;
    }
}
