package run.halo.app.extension;

import java.util.List;
import java.util.function.Function;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;
import run.halo.app.extension.index.query.Condition;
import run.halo.app.extension.index.query.LabelCondition;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;

@Data
@Accessors(chain = true)
public class ListOptions {

    private LabelSelector labelSelector;

    private FieldSelector fieldSelector;

    @Override
    public String toString() {
        return toCondition().toString();
    }

    /**
     * Convert to a single condition.
     *
     * @return the condition, never null
     */
    @NonNull
    public Condition toCondition() {
        Condition condition = null;
        var fieldSelector = getFieldSelector();
        if (fieldSelector != null && fieldSelector.query() != null) {
            var query = fieldSelector.query();
            if (!(query instanceof Condition fieldCondition)) {
                throw new IllegalArgumentException("Only support condition query");
            }
            condition = fieldCondition;
        }
        var labelSelector = getLabelSelector();
        if (labelSelector != null) {
            var labelCondition = labelSelector.getConditions().stream()
                .map(Function.<Condition>identity())
                .reduce(Condition::and)
                .orElse(null);
            if (labelCondition != null) {
                if (condition == null) {
                    condition = labelCondition;
                } else {
                    condition = condition.and(labelCondition);
                }
            }
        }
        return condition == null ? Condition.empty() : condition;
    }

    public static ListOptionsBuilder builder() {
        return new ListOptionsBuilder();
    }

    public static ListOptionsBuilder builder(ListOptions listOptions) {
        return new ListOptionsBuilder(listOptions);
    }

    public static class ListOptionsBuilder {
        private LabelSelectorBuilder labelSelectorBuilder;
        private Query query;

        public ListOptionsBuilder() {
        }

        /**
         * Create a new list options builder with the given list options.
         */
        public ListOptionsBuilder(ListOptions listOptions) {
            if (listOptions.getLabelSelector() != null) {
                this.labelSelectorBuilder = new LabelSelectorBuilder(
                    listOptions.getLabelSelector().getConditions(), this);
            }
            if (listOptions.getFieldSelector() != null) {
                this.query = listOptions.getFieldSelector().query();
            }
        }

        /**
         * Create a new label selector builder.
         */
        public LabelSelectorBuilder labelSelector() {
            if (labelSelectorBuilder == null) {
                labelSelectorBuilder = new LabelSelectorBuilder(this);
            }
            return labelSelectorBuilder;
        }

        public ListOptionsBuilder fieldQuery(Query query) {
            this.query = query;
            return this;
        }

        /**
         * And the given query to the current query.
         */
        public ListOptionsBuilder andQuery(Query query) {
            if (!(query instanceof Condition condition)) {
                throw new IllegalArgumentException("Given query must be an instance of Condition");
            }
            if (this.query == null) {
                this.query = condition;
            } else {
                if (!(this.query instanceof Condition currentCondition)) {
                    throw new IllegalArgumentException(
                        "Current query must be an instance of Condition"
                    );
                }
                this.query = currentCondition.and(condition);
            }
            return this;
        }

        /**
         * Or the given query to the current query.
         */
        public ListOptionsBuilder orQuery(Query query) {
            if (!(query instanceof Condition condition)) {
                throw new IllegalArgumentException("Given query must be an instance of Condition");
            }
            if (this.query == null) {
                this.query = condition;
            } else {
                if (!(this.query instanceof Condition currentCondition)) {
                    throw new IllegalArgumentException(
                        "Current query must be an instance of Condition"
                    );
                }
                this.query = currentCondition.or(condition);
            }
            return this;
        }

        /**
         * Build the list options.
         */
        public ListOptions build() {
            var listOptions = new ListOptions();
            if (labelSelectorBuilder != null) {
                listOptions.setLabelSelector(labelSelectorBuilder.build());
            }
            if (query != null) {
                listOptions.setFieldSelector(FieldSelector.of(query));
            }
            return listOptions;
        }
    }

    public static class LabelSelectorBuilder
        extends LabelSelector.LabelSelectorBuilder<LabelSelectorBuilder> {
        private final ListOptionsBuilder listOptionsBuilder;

        public LabelSelectorBuilder(List<LabelCondition> conditions,
            ListOptionsBuilder listOptionsBuilder) {
            super(conditions);
            this.listOptionsBuilder = listOptionsBuilder;
        }

        public LabelSelectorBuilder(ListOptionsBuilder listOptionsBuilder) {
            this.listOptionsBuilder = listOptionsBuilder;
        }

        public ListOptionsBuilder end() {
            return this.listOptionsBuilder;
        }
    }
}
