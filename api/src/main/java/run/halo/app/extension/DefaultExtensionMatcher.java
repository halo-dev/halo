package run.halo.app.extension;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;

@Getter
@RequiredArgsConstructor
@Builder(builderMethodName = "internalBuilder")
public class DefaultExtensionMatcher implements ExtensionMatcher {
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    private final GroupVersionKind gvk;
    private final LabelSelector labelSelector;
    private final FieldSelector fieldSelector;

    public static DefaultExtensionMatcherBuilder builder(GroupVersionKind gvk) {
        return internalBuilder().gvk(gvk);
    }

    /**
     * Match the given extension with the current matcher.
     *
     * @param extension extension to match
     * @return true if the extension matches the current matcher
     */
    @Override
    public boolean match(Extension extension) {
        if (gvk != null && !gvk.equals(extension.groupVersionKind())) {
            return false;
        }
        var labels = defaultIfNull(extension.getMetadata().getLabels(), Map.<String, String>of());
        if (labelSelector != null && !labelSelector.test(labels)) {
            return false;
        }

        if (fieldSelector != null) {
            for (var matcher : fieldSelector.getMatchers()) {
                var fieldValue = PARSER.parseRaw(matcher.getKey())
                    .getValue(extension, String.class);
                if (!matcher.test(fieldValue)) {
                    return false;
                }
            }
        }
        return true;
    }
}
