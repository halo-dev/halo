package run.halo.app.extension.index.query;

import java.util.Set;
import org.springframework.util.Assert;

record LabelNotInCondition(String labelKey, Set<String> labelValues) implements LabelCondition {

    public LabelNotInCondition {
        Assert.notNull(labelValues, "labelValues of " + labelKey + " must not be null");
    }

    @Override
    public Condition not() {
        return new LabelInCondition(labelKey, labelValues);
    }

}
