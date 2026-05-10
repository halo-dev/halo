package run.halo.app.theme.dialect;

import com.github.zafarkhaja.semver.Version;
import java.util.Set;
import java.util.function.Supplier;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;
import run.halo.app.theme.dialect.expression.Annotations;
import run.halo.app.theme.dialect.expression.HaloExpressionObject;

/**
 * Builds the expression objects to be used by Halo dialects.
 *
 * @author guqing
 * @since 2.0.0
 */
public class HaloExpressionObjectFactory implements IExpressionObjectFactory {

    public static final String ANNOTATIONS_EXPRESSION_OBJECT_NAME = "annotations";
    public static final String HALO_EXPRESSION_OBJECT_NAME = "halo";

    protected static final Set<String> ALL_EXPRESSION_OBJECT_NAMES =
            Set.of(ANNOTATIONS_EXPRESSION_OBJECT_NAME, HALO_EXPRESSION_OBJECT_NAME);

    private static final Annotations ANNOTATIONS = new Annotations();

    private final Supplier<Version> versionSupplier;

    public HaloExpressionObjectFactory() {
        this(null);
    }

    public HaloExpressionObjectFactory(Supplier<Version> versionSupplier) {
        this.versionSupplier = versionSupplier;
    }

    @Override
    public Set<String> getAllExpressionObjectNames() {
        return ALL_EXPRESSION_OBJECT_NAMES;
    }

    @Override
    public Object buildObject(IExpressionContext context, String expressionObjectName) {
        if (ANNOTATIONS_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            return ANNOTATIONS;
        }
        if (HALO_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            return new HaloExpressionObject(versionSupplier);
        }
        return null;
    }

    @Override
    public boolean isCacheable(String expressionObjectName) {
        return true;
    }
}
