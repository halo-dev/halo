package run.halo.app.extension.index.query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

public abstract class SimpleQuery implements Query {
    protected final String fieldName;
    protected final String value;
    protected final boolean isFieldRef;

    protected SimpleQuery(String fieldName, String value) {
        this(fieldName, value, false);
    }

    protected SimpleQuery(String fieldName, String value, boolean isFieldRef) {
        Assert.isTrue(StringUtils.isNotBlank(fieldName), "fieldName cannot be blank.");
        this.fieldName = fieldName;
        this.value = value;
        this.isFieldRef = isFieldRef;
    }
}
