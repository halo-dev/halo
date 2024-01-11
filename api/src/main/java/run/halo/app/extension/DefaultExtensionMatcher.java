package run.halo.app.extension;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;

@Getter
@RequiredArgsConstructor
@Builder(builderMethodName = "internalBuilder")
public class DefaultExtensionMatcher implements ExtensionMatcher {
    private final ExtensionClient client;
    private final GroupVersionKind gvk;
    private final LabelSelector labelSelector;
    private final FieldSelector fieldSelector;

    public static DefaultExtensionMatcherBuilder builder(ExtensionClient client,
        GroupVersionKind gvk) {
        return internalBuilder().client(client).gvk(gvk);
    }

    /**
     * Match the given extension with the current matcher.
     *
     * @param extension extension to match
     * @return true if the extension matches the current matcher
     */
    @Override
    public boolean match(Extension extension) {
        if (!gvk.equals(extension.groupVersionKind())) {
            return false;
        }
        if (!hasFieldSelector() && !hasLabelSelector()) {
            return true;
        }
        var listOptions = new ListOptions();
        listOptions.setLabelSelector(labelSelector);
        var fieldQuery = QueryFactory.all();
        if (hasFieldSelector()) {
            fieldQuery = QueryFactory.and(fieldQuery, fieldSelector.query());
        }
        listOptions.setFieldSelector(new FieldSelector(fieldQuery));
        return client.indexedQueryEngine().retrieve(getGvk(),
            listOptions, PageRequestImpl.ofSize(1)).getTotal() > 0;
    }

    boolean hasFieldSelector() {
        return fieldSelector != null && fieldSelector.query() != null;
    }

    boolean hasLabelSelector() {
        return labelSelector != null && !CollectionUtils.isEmpty(labelSelector.getMatchers());
    }
}
