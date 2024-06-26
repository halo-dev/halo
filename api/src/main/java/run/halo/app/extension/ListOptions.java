package run.halo.app.extension;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;
import run.halo.app.extension.router.selector.SelectorMatcher;

@Data
@Accessors(chain = true)
public class ListOptions {
    private LabelSelector labelSelector;
    private FieldSelector fieldSelector;

    @Override
    public String toString() {
        var sb = new StringBuilder();
        if (fieldSelector != null) {
            var query = fieldSelector.query().toString();
            sb.append("fieldSelector: ")
                .append(query.startsWith("(") ? query : "(" + query + ")");
        }
        if (labelSelector != null) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append("labelSelector: (").append(labelSelector).append(")");
        }
        return sb.toString();
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
                    listOptions.getLabelSelector().getMatchers(), this);
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
            this.query = (this.query == null ? query : QueryFactory.and(this.query, query));
            return this;
        }

        /**
         * Or the given query to the current query.
         */
        public ListOptionsBuilder orQuery(Query query) {
            this.query = (this.query == null ? query : QueryFactory.or(this.query, query));
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

        public LabelSelectorBuilder(List<SelectorMatcher> givenMatchers,
            ListOptionsBuilder listOptionsBuilder) {
            super(givenMatchers);
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
