package run.halo.app.extension.router.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FieldSelector implements Predicate<String> {
    private List<SelectorMatcher> matchers;

    @Override
    public boolean test(String fieldValue) {
        if (matchers == null || matchers.isEmpty()) {
            return true;
        }
        return matchers.stream()
            .allMatch(matcher -> matcher.test(fieldValue));
    }

    public static FieldSelectorBuilder builder() {
        return new FieldSelectorBuilder();
    }

    public static class FieldSelectorBuilder {
        private final List<SelectorMatcher> matchers = new ArrayList<>();

        public FieldSelectorBuilder eq(String fieldPath, String value) {
            matchers.add(EqualityMatcher.equal(fieldPath, value));
            return this;
        }

        public FieldSelectorBuilder notEq(String fieldPath, String value) {
            matchers.add(EqualityMatcher.notEqual(fieldPath, value));
            return this;
        }

        /**
         * Build a field selector.
         */
        public FieldSelector build() {
            FieldSelector fieldSelector = new FieldSelector();
            fieldSelector.setMatchers(matchers);
            return fieldSelector;
        }
    }
}
