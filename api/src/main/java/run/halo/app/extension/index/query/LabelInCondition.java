package run.halo.app.extension.index.query;

import java.util.Set;
import org.springframework.util.Assert;

record LabelInCondition(String labelKey, Set<String> labelValues) implements LabelCondition {

    public LabelInCondition {
        Assert.notNull(labelValues, "labelValues of " + labelKey + " must not be null");
    }

    @Override
    public Condition not() {
        return new LabelNotInCondition(labelKey, labelValues);
    }

}
