package run.halo.app.theme.dialect;

import static run.halo.app.theme.ThemeLocaleContextResolver.TIME_ZONE_REQUEST_ATTRIBUTE_NAME;

import java.util.TimeZone;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.extras.java8time.dialect.Java8TimeExpressionFactory;
import org.thymeleaf.extras.java8time.expression.Temporals;

/**
 * @author guqing
 * @since 2.0.0
 */
public class DefaultJava8TimeExpressionFactory extends Java8TimeExpressionFactory {
    private static final String TEMPORAL_EVALUATION_VARIABLE_NAME = "temporals";

    @Override
    public Object buildObject(IExpressionContext context, String expressionObjectName) {
        TimeZone timeZone = (TimeZone) context.getVariable(TIME_ZONE_REQUEST_ATTRIBUTE_NAME);
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        if (TEMPORAL_EVALUATION_VARIABLE_NAME.equals(expressionObjectName)) {
            return new Temporals(context.getLocale(), timeZone.toZoneId());
        }
        return null;
    }
}
