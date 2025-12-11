package run.halo.app.extension.index.query;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

record LabelNotInCondition(String labelKey, Collection<String> labelValues)
    implements LabelCondition {

    public LabelNotInCondition {
        Assert.notNull(labelValues, "labelValues of " + labelKey + " must not be null");
    }

    @Override
    public LabelCondition not() {
        return new LabelInCondition(labelKey, labelValues);
    }

    @NotNull
    @Override
    public String toString() {
        return INDEX_NAME + "['" + labelKey + "'] NOT IN ("
            + String.join(", ", labelValues.stream().map(v -> "'" + v + "'").toList())
            + ")";
    }
}
