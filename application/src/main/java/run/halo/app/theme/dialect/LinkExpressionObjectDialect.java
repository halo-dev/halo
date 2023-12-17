package run.halo.app.theme.dialect;

import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

/**
 * An expression object dialect for theme link.
 *
 * @author guqing
 * @since 2.0.0
 */
public class LinkExpressionObjectDialect extends AbstractDialect implements
    IExpressionObjectDialect {

    private static final IExpressionObjectFactory LINK_EXPRESSION_OBJECTS_FACTORY =
        new DefaultLinkExpressionFactory();

    public LinkExpressionObjectDialect() {
        super("themeLink");
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return LINK_EXPRESSION_OBJECTS_FACTORY;
    }
}
