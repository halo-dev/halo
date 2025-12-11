package run.halo.app.extension.index.query;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

record LabelInCondition(String labelKey, Collection<String> labelValues) implements LabelCondition {

    public LabelInCondition {
        Assert.notNull(labelValues, "labelValues of " + labelKey + " must not be null");
    }

    @Override
    public LabelCondition not() {
        return new LabelNotInCondition(labelKey, labelValues);
    }

    @NotNull
    @Override
    public String toString() {
        return INDEX_NAME + "['" + labelKey + "'] IN ("
            + String.join(", ", labelValues.stream().map(v -> "'" + v + "'").toList())
            + ")";
    }
}
