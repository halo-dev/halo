package run.halo.app.theme.dialect;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.CompilablePropertyAccessor;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.integration.json.JsonPropertyAccessor;
import org.springframework.lang.Nullable;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.ITemplateEnd;
import org.thymeleaf.model.ITemplateStart;
import org.thymeleaf.processor.templateboundaries.AbstractTemplateBoundariesProcessor;
import org.thymeleaf.processor.templateboundaries.ITemplateBoundariesStructureHandler;
import org.thymeleaf.spring6.expression.ThymeleafEvaluationContext;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templatemode.TemplateMode;
import run.halo.app.infra.utils.ReactiveUtils;

/**
 * Enhance the evaluation context to support reactive types.
 *
 * @author guqing
 * @author johnniang
 * @since 2.20.0
 */
public class EvaluationContextEnhancer extends AbstractTemplateBoundariesProcessor {

    private static final int PRECEDENCE = StandardDialect.PROCESSOR_PRECEDENCE;

    private static final JsonPropertyAccessor JSON_PROPERTY_ACCESSOR = new JsonPropertyAccessor();

    public EvaluationContextEnhancer() {
        super(TemplateMode.HTML, PRECEDENCE);
    }

    @Override
    public void doProcessTemplateStart(ITemplateContext context, ITemplateStart templateStart,
        ITemplateBoundariesStructureHandler structureHandler) {
        var evluationContextObject = context.getVariable(
            ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME
        );
        if (evluationContextObject instanceof ThymeleafEvaluationContext evaluationContext) {
            evaluationContext.addPropertyAccessor(JSON_PROPERTY_ACCESSOR);
            ReactiveReflectivePropertyAccessor.wrap(evaluationContext);
            ReactiveMethodResolver.wrap(evaluationContext);
        }
    }

    @Override
    public void doProcessTemplateEnd(ITemplateContext context, ITemplateEnd templateEnd,
        ITemplateBoundariesStructureHandler structureHandler) {
        // nothing to do
    }

    /**
     * A {@link PropertyAccessor} that wraps the original {@link ReflectivePropertyAccessor} and
     * blocks the reactive value.
     */
    private static class ReactiveReflectivePropertyAccessor
        extends ReflectivePropertyAccessor {
        private final ReflectivePropertyAccessor delegate;

        private ReactiveReflectivePropertyAccessor(ReflectivePropertyAccessor delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean canRead(EvaluationContext context, Object target, String name)
            throws AccessException {
            if (target == null) {
                // For backward compatibility
                return true;
            }
            return this.delegate.canRead(context, target, name);
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name)
            throws AccessException {
            if (target == null) {
                // For backward compatibility
                return TypedValue.NULL;
            }
            var typedValue = delegate.read(context, target, name);
            return Optional.of(typedValue)
                .filter(tv ->
                    Objects.nonNull(tv.getValue())
                        && Objects.nonNull(tv.getTypeDescriptor())
                        && ReactiveUtils.isReactiveType(tv.getTypeDescriptor().getType())
                )
                .map(tv -> new TypedValue(ReactiveUtils.blockReactiveValue(tv.getValue())))
                .orElse(typedValue);
        }

        @Override
        public boolean canWrite(EvaluationContext context, Object target, String name)
            throws AccessException {
            return delegate.canWrite(context, target, name);
        }

        @Override
        public void write(EvaluationContext context, Object target, String name, Object newValue)
            throws AccessException {
            delegate.write(context, target, name, newValue);
        }

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return delegate.getSpecificTargetClasses();
        }

        @Override
        public PropertyAccessor createOptimalAccessor(EvaluationContext context, Object target,
            String name) {
            var optimalAccessor = delegate.createOptimalAccessor(context, target, name);
            if (optimalAccessor instanceof CompilablePropertyAccessor optimalPropertyAccessor) {
                if (ReactiveUtils.isReactiveType(optimalPropertyAccessor.getPropertyType())) {
                    return this;
                }
                return optimalPropertyAccessor;
            }
            return this;
        }

        static void wrap(ThymeleafEvaluationContext evaluationContext) {
            var wrappedPropertyAccessors = evaluationContext.getPropertyAccessors()
                .stream()
                .map(propertyAccessor -> {
                    if (propertyAccessor instanceof ReflectivePropertyAccessor reflectiveAccessor) {
                        return new ReactiveReflectivePropertyAccessor(reflectiveAccessor);
                    }
                    return propertyAccessor;
                })
                // make the list mutable
                .collect(Collectors.toCollection(ArrayList::new));
            evaluationContext.setPropertyAccessors(wrappedPropertyAccessors);
        }

        @Override
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

    }

    /**
     * A {@link MethodResolver} that wraps the original {@link MethodResolver} and blocks the
     * reactive value.
     *
     * @param delegate the original {@link MethodResolver}
     */
    private record ReactiveMethodResolver(MethodResolver delegate) implements MethodResolver {

        @Override
        @Nullable
        public MethodExecutor resolve(EvaluationContext context, Object targetObject, String name,
            List<TypeDescriptor> argumentTypes) throws AccessException {
            var executor = delegate.resolve(context, targetObject, name, argumentTypes);
            return Optional.ofNullable(executor).map(ReactiveMethodExecutor::new).orElse(null);
        }

        static void wrap(ThymeleafEvaluationContext evaluationContext) {
            var wrappedMethodResolvers = evaluationContext.getMethodResolvers()
                .stream()
                .<MethodResolver>map(ReactiveMethodResolver::new)
                // make the list mutable
                .collect(Collectors.toCollection(ArrayList::new));
            evaluationContext.setMethodResolvers(wrappedMethodResolvers);
        }

    }

    /**
     * A {@link MethodExecutor} that wraps the original {@link MethodExecutor} and blocks the
     * reactive value.
     *
     * @param delegate the original {@link MethodExecutor}
     */
    private record ReactiveMethodExecutor(MethodExecutor delegate) implements MethodExecutor {

        @Override
        public TypedValue execute(EvaluationContext context, Object target, Object... arguments)
            throws AccessException {
            var typedValue = delegate.execute(context, target, arguments);
            return Optional.of(typedValue)
                .filter(tv ->
                    Objects.nonNull(tv.getValue())
                        && Objects.nonNull(tv.getTypeDescriptor())
                        && ReactiveUtils.isReactiveType(tv.getTypeDescriptor().getType())
                )
                .map(tv -> new TypedValue(ReactiveUtils.blockReactiveValue(tv.getValue())))
                .orElse(typedValue);
        }

    }
}
