package run.halo.app.extension.index.query;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

@Getter
public abstract class SimpleQuery implements Query {
    protected final String fieldName;
    protected final String value;
    /**
     * <p>Whether the value if a field reference.</p>
     * For example, {@code fieldName = "salary", value = "cost"} can lead to a query：
     * <pre>
     *     salary > cost
     * </pre>
     * means that we want to find all the records whose salary is greater than cost.
     *
     * @see EqualQuery
     * @see GreaterThanQuery
     * @see LessThanQuery
     */
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
