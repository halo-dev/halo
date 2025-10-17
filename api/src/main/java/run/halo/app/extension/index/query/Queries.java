package run.halo.app.extension.index.query;

import java.util.Set;
import org.springframework.util.Assert;

public enum Queries {
    ;

    public static Condition between(String fieldName,
        Object fromValue, boolean fromInclusive, Object toValue, boolean toInclusive) {
        return new BetweenCondition(fieldName, fromValue, fromInclusive, toValue, toInclusive);
    }

    public static Condition empty() {
        return new EmptyCondition();
    }

    public static Condition all(String fieldName) {
        return new AllCondition(fieldName);
    }

    public static Condition equal(String fieldName, Object attributeValue) {
        Assert.notNull(attributeValue,
            "Attribute key of field " + fieldName + " must not be null"
        );
        return new EqualCondition(fieldName, attributeValue);
    }

    public static Condition greaterThan(
        String fieldName, Object attributeValue, boolean inclusive) {
        return new GreaterThanCondition(fieldName, attributeValue, inclusive);
    }

    public static Condition greaterThan(String fieldName, Object attributeValue) {
        return greaterThan(fieldName, attributeValue, false);
    }

    public static Condition in(String fieldName, Object... attributeValues) {
        return in(fieldName, Set.of(attributeValues));
    }

    public static Condition in(String fieldName, Set<Object> values) {
        Assert.notNull(values, "Values must not be null");
        if (values.size() == 1) {
            var value = values.iterator().next();
            return equal(fieldName, value);
        }
        return new InCondition(fieldName, values);
    }

    public static Condition lessThan(String fieldName, Object attributeValue, boolean inclusive) {
        return new LessThanCondition(fieldName, attributeValue, inclusive);
    }

    public static Condition lessThan(String fieldName, Object attributeValue) {
        return lessThan(fieldName, attributeValue, false);
    }

    public static Condition notEqual(String fieldName, Object attributeValue) {
        return new NotEqualCondition(fieldName, attributeValue);
    }

    public static Condition startsWith(String fieldName, String prefix) {
        return new StringStartsWithCondition(fieldName, prefix);
    }

    public static Condition endsWith(String fieldName, String suffix) {
        return new StringEndsWithCondition(fieldName, suffix);
    }

    public static Condition contains(String fieldName, String substring) {
        return new StringContainsCondition(fieldName, substring);
    }

}
