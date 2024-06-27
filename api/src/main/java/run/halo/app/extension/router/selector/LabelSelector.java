package run.halo.app.extension.router.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

@Data
@Accessors(chain = true)
public class LabelSelector implements Predicate<Map<String, String>> {
    private List<SelectorMatcher> matchers;

    @Override
    public boolean test(@NonNull Map<String, String> labels) {
        Assert.notNull(labels, "Labels must not be null");
        if (matchers == null || matchers.isEmpty()) {
            return true;
        }
        return matchers.stream()
            .allMatch(matcher -> matcher.test(labels.get(matcher.getKey())));
    }

    @Override
    public String toString() {
        if (matchers == null || matchers.isEmpty()) {
            return "";
        }
        return matchers.stream()
            .map(SelectorMatcher::toString)
            .collect(Collectors.joining(", "));
    }

    /**
     * Returns a new label selector that is the result of ANDing the current selector with the
     * given selector.
     *
     * @param other the selector to AND with
     * @return a new label selector
     */
    public LabelSelector and(LabelSelector other) {
        var labelSelector = new LabelSelector();
        var matchers = new ArrayList<SelectorMatcher>();
        matchers.addAll(this.matchers);
        matchers.addAll(other.matchers);
        labelSelector.setMatchers(matchers);
        return labelSelector;
    }

    public static LabelSelectorBuilder<?> builder() {
        return new LabelSelectorBuilder<>();
    }

    public static class LabelSelectorBuilder<T extends LabelSelectorBuilder<T>> {
        private final List<SelectorMatcher> matchers = new ArrayList<>();

        public LabelSelectorBuilder() {
        }

        /**
         * Create a new label selector builder with the given matchers.
         */
        public LabelSelectorBuilder(List<SelectorMatcher> givenMatchers) {
            if (givenMatchers != null) {
                matchers.addAll(givenMatchers);
            }
        }

        @SuppressWarnings("unchecked")
        private T self() {
            return (T) this;
        }

        public T eq(String key, String value) {
            matchers.add(EqualityMatcher.equal(key, value));
            return self();
        }

        public T notEq(String key, String value) {
            matchers.add(EqualityMatcher.notEqual(key, value));
            return self();
        }

        public T in(String key, String... values) {
            matchers.add(SetMatcher.in(key, values));
            return self();
        }

        public T notIn(String key, String... values) {
            matchers.add(SetMatcher.notIn(key, values));
            return self();
        }

        public T exists(String key) {
            matchers.add(SetMatcher.exists(key));
            return self();
        }

        public T notExists(String key) {
            matchers.add(SetMatcher.notExists(key));
            return self();
        }

        /**
         * Build the label selector.
         */
        public LabelSelector build() {
            var labelSelector = new LabelSelector();
            labelSelector.setMatchers(matchers);
            return labelSelector;
        }
    }
}
