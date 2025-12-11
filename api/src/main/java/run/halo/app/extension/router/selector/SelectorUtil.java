package run.halo.app.extension.router.selector;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.index.query.Condition;

public final class SelectorUtil {

    private SelectorUtil() {
    }

    /**
     * Convert label and field selector expressions to {@link ListOptions}.
     *
     * @param labelSelectorTerms label selector expressions
     * @param fieldSelectorTerms field selector expressions
     * @return list options(never null)
     */
    public static ListOptions labelAndFieldSelectorToListOptions(
        List<String> labelSelectorTerms, List<String> fieldSelectorTerms) {
        var selectorConverter = new SelectorConverter();

        var labelConverter = new LabelSelectorConverter();
        var labelConditions = Optional.ofNullable(labelSelectorTerms)
            .map(selectors -> selectors.stream()
                .map(selectorConverter::convert)
                .filter(Objects::nonNull)
                .map(labelConverter::convert)
                .toList())
            .orElse(List.of());

        var fieldConverter = new FieldSelectorConverter();
        var fieldCondition = Optional.ofNullable(fieldSelectorTerms)
            .map(selectors -> selectors.stream()
                .map(selectorConverter::convert)
                .filter(Objects::nonNull)
                .map(fieldConverter::convert)
                .reduce(Condition::and)
                .orElse(null)
            )
            .orElse(null);
        var listOptions = new ListOptions();
        listOptions.setLabelSelector(new LabelSelector().setConditions(labelConditions));
        if (fieldCondition != null) {
            listOptions.setFieldSelector(FieldSelector.of(fieldCondition));
        } else {
            listOptions.setFieldSelector(FieldSelector.all());
        }
        return listOptions;
    }
}
