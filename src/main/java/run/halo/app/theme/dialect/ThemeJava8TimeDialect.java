package run.halo.app.theme.dialect;

import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;

/**
 * @author guqing
 * @since 2.0.0
 */
public class ThemeJava8TimeDialect extends Java8TimeDialect {
    private final IExpressionObjectFactory expressionObjectFactory =
        new DefaultJava8TimeExpressionFactory();

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return expressionObjectFactory;
    }
}
