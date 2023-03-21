package run.halo.app.theme.dialect;

import java.util.Set;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.linkbuilder.ILinkBuilder;
import org.thymeleaf.util.Validate;
import run.halo.app.theme.ThemeLinkBuilder;

/**
 * A default implementation of {@link IExpressionObjectFactory}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class DefaultLinkExpressionFactory implements IExpressionObjectFactory {
    private static final String THEME_EVALUATION_VARIABLE_NAME = "theme";

    @Override
    public Set<String> getAllExpressionObjectNames() {
        return Set.of(THEME_EVALUATION_VARIABLE_NAME);
    }

    @Override
    public Object buildObject(IExpressionContext context, String expressionObjectName) {
        if (THEME_EVALUATION_VARIABLE_NAME.equals(expressionObjectName)) {
            return new ThemeLinkExpressObject(context);
        }
        return null;
    }

    @Override
    public boolean isCacheable(String expressionObjectName) {
        return THEME_EVALUATION_VARIABLE_NAME.equals(expressionObjectName);
    }

    public static class ThemeLinkExpressObject {
        private final ILinkBuilder linkBuilder;
        private final IExpressionContext context;

        /**
         * Construct an expression object that provides a set of methods to handle link in
         * Javascript or HTML through {@link IExpressionContext}.
         *
         * @param context expression context
         */
        public ThemeLinkExpressObject(IExpressionContext context) {
            Validate.notNull(context, "Context cannot be null");
            this.context = context;
            Set<ILinkBuilder> linkBuilders = context.getConfiguration().getLinkBuilders();
            linkBuilder = linkBuilders.stream()
                .findFirst()
                .orElseThrow(() -> new TemplateProcessingException("Link builder not found"));
        }

        public String assets(String path) {
            String assetsPath = ThemeLinkBuilder.THEME_ASSETS_PREFIX + path;
            return linkBuilder.buildLink(context, assetsPath, null);
        }

        public String route(String path) {
            return linkBuilder.buildLink(context, path, null);
        }
    }
}
