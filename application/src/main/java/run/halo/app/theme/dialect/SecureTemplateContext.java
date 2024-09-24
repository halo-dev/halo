package run.halo.app.theme.dialect;

import static org.thymeleaf.spring6.expression.ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.IdentifierSequences;
import org.thymeleaf.engine.TemplateData;
import org.thymeleaf.expression.IExpressionObjects;
import org.thymeleaf.inline.IInliner;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Secure template context.
 *
 * @author johnniang
 * @since 2.20.0
 */
class SecureTemplateContext implements ITemplateContext {

    private static final Set<String> DANGEROUS_VARIABLES =
        Set.of(THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME);

    private final ITemplateContext delegate;

    public SecureTemplateContext(ITemplateContext delegate) {
        this.delegate = delegate;
    }

    @Override
    public TemplateData getTemplateData() {
        return delegate.getTemplateData();
    }

    @Override
    public TemplateMode getTemplateMode() {
        return delegate.getTemplateMode();
    }

    @Override
    public List<TemplateData> getTemplateStack() {
        return delegate.getTemplateStack();
    }

    @Override
    public List<IProcessableElementTag> getElementStack() {
        return delegate.getElementStack();
    }

    @Override
    public Map<String, Object> getTemplateResolutionAttributes() {
        return delegate.getTemplateResolutionAttributes();
    }

    @Override
    public IModelFactory getModelFactory() {
        return delegate.getModelFactory();
    }

    @Override
    public boolean hasSelectionTarget() {
        return delegate.hasSelectionTarget();
    }

    @Override
    public Object getSelectionTarget() {
        return delegate.getSelectionTarget();
    }

    @Override
    public IInliner getInliner() {
        return delegate.getInliner();
    }

    @Override
    public String getMessage(
        Class<?> origin,
        String key,
        Object[] messageParameters,
        boolean useAbsentMessageRepresentation
    ) {
        return delegate.getMessage(origin, key, messageParameters, useAbsentMessageRepresentation);
    }

    @Override
    public String buildLink(String base, Map<String, Object> parameters) {
        return delegate.buildLink(base, parameters);
    }

    @Override
    public IdentifierSequences getIdentifierSequences() {
        return delegate.getIdentifierSequences();
    }

    @Override
    public IEngineConfiguration getConfiguration() {
        return delegate.getConfiguration();
    }

    @Override
    public IExpressionObjects getExpressionObjects() {
        return delegate.getExpressionObjects();
    }

    @Override
    public Locale getLocale() {
        return delegate.getLocale();
    }

    @Override
    public boolean containsVariable(String name) {
        if (DANGEROUS_VARIABLES.contains(name)) {
            return false;
        }
        return delegate.containsVariable(name);
    }

    @Override
    public Set<String> getVariableNames() {
        return delegate.getVariableNames()
            .stream()
            .filter(name -> !DANGEROUS_VARIABLES.contains(name))
            .collect(Collectors.toSet());
    }

    @Override
    public Object getVariable(String name) {
        if (DANGEROUS_VARIABLES.contains(name)) {
            return null;
        }
        return delegate.getVariable(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SecureTemplateContext that = (SecureTemplateContext) o;
        return Objects.equals(delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(delegate);
    }
}
