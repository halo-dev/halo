package run.halo.app.extension.router.selector;

import java.util.Objects;
import java.util.function.UnaryOperator;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

@Data
@Accessors(chain = true)
public class FieldSelector {
    private SelectorMatcher matcher;

    public static FieldSelectorBuilder builder() {
        return new FieldSelectorBuilder(null);
    }

    public static FieldSelectorBuilder builder(SelectorMatcher rootMatcher) {
        return new FieldSelectorBuilder(rootMatcher);
    }

    public boolean test(UnaryOperator<String> valueForKeyFunc) {
        return evaluate(matcher, valueForKeyFunc);
    }

    boolean evaluate(SelectorMatcher matcher, UnaryOperator<String> valueForKeyFunc) {
        if (matcher instanceof LogicalMatcher) {
            if (matcher instanceof AndSelectorMatcher andNode) {
                return evaluate(andNode.getLeft(), valueForKeyFunc)
                    && evaluate(andNode.getRight(), valueForKeyFunc);
            } else if (matcher instanceof OrSelectorMatcher orNode) {
                return evaluate(orNode.getLeft(), valueForKeyFunc)
                    || evaluate(orNode.getRight(), valueForKeyFunc);
            } else if (matcher instanceof AnySelectorMatcher) {
                return true;
            }
        }
        String valueToTest = valueForKeyFunc.apply(matcher.getKey());
        return matcher.test(valueToTest);
    }

    public static class FieldSelectorBuilder {
        private SelectorMatcher rootMatcher;

        public FieldSelectorBuilder(SelectorMatcher rootMatcher) {
            this.rootMatcher = rootMatcher;
        }

        public FieldSelectorBuilder eq(String fieldName, String fieldValue) {
            return and(EqualityMatcher.equal(fieldName, fieldValue));
        }

        public FieldSelectorBuilder notEq(String fieldName, String fieldValue) {
            return and(EqualityMatcher.notEqual(fieldName, fieldValue));
        }

        public FieldSelectorBuilder in(String fieldName, String... fieldValues) {
            return and(SetMatcher.in(fieldName, fieldValues));
        }

        public FieldSelectorBuilder notIn(String fieldName, String... fieldValues) {
            return and(SetMatcher.notIn(fieldName, fieldValues));
        }

        public FieldSelectorBuilder exists(String fieldName) {
            return and(SetMatcher.exists(fieldName));
        }

        public FieldSelectorBuilder notExists(String fieldName) {
            return and(SetMatcher.notExists(fieldName));
        }

        /**
         * Combine the current selector matcher with another one with AND.
         *
         * @param other another selector matcher to be combined with the current one with AND
         * @return the current selector matcher builder
         */
        public FieldSelectorBuilder and(SelectorMatcher other) {
            Assert.notNull(other, "Other selector matcher must not be null");
            if (rootMatcher == null) {
                this.rootMatcher = other;
                return this;
            }
            this.rootMatcher = new AndSelectorMatcher(rootMatcher, other);
            return this;
        }

        /**
         * Combine the current selector matcher with another one with OR.
         *
         * @param other another selector matcher to be combined with the current one with OR
         * @return the current selector matcher builder
         */
        public FieldSelectorBuilder or(SelectorMatcher other) {
            Assert.notNull(other, "Other selector matcher must not be null");
            if (rootMatcher == null) {
                rootMatcher = other;
            }
            rootMatcher = new OrSelectorMatcher(rootMatcher, other);
            return this;
        }

        /**
         * Build the selector matcher.
         */
        public FieldSelector build() {
            var fieldSelector = new FieldSelector();
            fieldSelector.setMatcher(buildMatcher());
            return fieldSelector;
        }

        public SelectorMatcher buildMatcher() {
            return Objects.requireNonNullElseGet(rootMatcher, AnySelectorMatcher::new);
        }
    }
}
