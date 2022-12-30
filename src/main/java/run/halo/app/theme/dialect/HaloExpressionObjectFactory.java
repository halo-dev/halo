package run.halo.app.theme.dialect;

import java.util.Set;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;
import run.halo.app.theme.dialect.expression.Annotations;

/**
 * Builds the expression objects to be used by Halo dialects.
 *
 * @author guqing
 * @since 2.0.0
 */
public class HaloExpressionObjectFactory implements IExpressionObjectFactory {

    public static final String ANNOTATIONS_EXPRESSION_OBJECT_NAME = "annotations";

    protected static final Set<String> ALL_EXPRESSION_OBJECT_NAMES = Set.of(
        ANNOTATIONS_EXPRESSION_OBJECT_NAME);

    private static final Annotations ANNOTATIONS = new Annotations();

    @Override
    public Set<String> getAllExpressionObjectNames() {
        return ALL_EXPRESSION_OBJECT_NAMES;
    }

    @Override
    public Object buildObject(IExpressionContext context, String expressionObjectName) {
        if (ANNOTATIONS_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            return ANNOTATIONS;
        }
        return null;
    }

    @Override
    public boolean isCacheable(String expressionObjectName) {
        return true;
    }
}
