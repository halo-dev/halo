package run.halo.app.extension.index.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.springframework.util.Assert;

/**
 * A utility class for building query conditions.
 *
 * <p>
 * Use {@link Condition#not()} to create negated conditions.
 *
 * @author johnniang
 * @since 2.22.0
 */
public enum Queries {
    ;

    /**
     * Combines multiple conditions with a logical AND.
     *
     * @param condition the first condition, must not be null
     * @param additionalConditions additional conditions to combine
     * @return the combined condition
     */
    public static Condition and(Condition condition, Condition... additionalConditions) {
        Assert.notNull(condition, "Condition must not be null");
        return Arrays.stream(additionalConditions)
            .reduce(condition, Condition::and);
    }

    /**
     * Combines multiple conditions with a logical OR.
     *
     * @param condition the first condition, must not be null
     * @param additionalConditions additional conditions to combine
     * @return the combined condition
     */
    public static Condition or(Condition condition, Condition... additionalConditions) {
        Assert.notNull(condition, "Condition must not be null");
        return Arrays.stream(additionalConditions)
            .reduce(condition, Condition::or);
    }

    /**
     * Negates the specified condition.
     *
     * @param condition the condition to negate, must not be null
     * @return the negated condition
     */
    public static Condition not(Condition condition) {
        Assert.notNull(condition, "Condition must not be null");
        return condition.not();
    }

    /**
     * Creates a "between" condition for the specified field name and range values.
     *
     * @param fieldName the name of the field
     * @param fromValue the start value of the range
     * @param fromInclusive whether the start value is inclusive
     * @param toValue the end value of the range
     * @param toInclusive whether the end value is inclusive
     * @return the "between" condition
     */
    public static Condition between(String fieldName,
        Object fromValue, boolean fromInclusive, Object toValue, boolean toInclusive) {
        return new BetweenCondition(fieldName, fromValue, fromInclusive, toValue, toInclusive);
    }

    /**
     * Creates an empty condition that matches all records.
     *
     * @return the empty condition
     */
    public static Condition empty() {
        return new EmptyCondition();
    }

    /**
     * Creates an "all" condition for the specified field name.
     *
     * @param fieldName the name of the field
     * @return the "all" condition
     */
    public static Condition all(String fieldName) {
        return new AllCondition(fieldName);
    }

    /**
     * Creates an "is null" condition for the specified field name.
     *
     * @param fieldName the name of the field
     * @return the "is null" condition
     */
    public static Condition isNull(String fieldName) {
        return new IsNullCondition(fieldName);
    }

    /**
     * Creates an "equal" condition for the specified field name and attribute value.
     *
     * @param fieldName the name of the field
     * @param attributeValue the attribute value, must not be null
     * @return the "equal" condition
     */
    public static Condition equal(String fieldName, Object attributeValue) {
        Assert.notNull(attributeValue,
            "Attribute key of field " + fieldName + " must not be null"
        );
        return new EqualCondition(fieldName, attributeValue);
    }

    /**
     * Creates a "greater than" condition for the specified field name and attribute value.
     *
     * @param fieldName the name of the field
     * @param attributeValue the attribute value, must not be null
     * @param inclusive whether the comparison is inclusive
     * @return the "greater than" condition
     */
    public static Condition greaterThan(
        String fieldName, Object attributeValue, boolean inclusive) {
        return new GreaterThanCondition(fieldName, attributeValue, inclusive);
    }

    /**
     * Creates a "greater than" condition for the specified field name and attribute value.
     *
     * @param fieldName the name of the field
     * @param attributeValue the attribute value, must not be null. Which is not inclusive.
     * @return the "greater than" condition
     */
    public static Condition greaterThan(String fieldName, Object attributeValue) {
        return greaterThan(fieldName, attributeValue, false);
    }

    /**
     * Creates an "in" condition for the specified field name and attribute values.
     *
     * @param fieldName the name of the field
     * @param attributeValue the first attribute value, must not be null. If it's a collection,
     * it will be treated as the collection of values for the "in" condition.
     * @param additionalValues additional attribute values
     * @return the "in" condition
     */
    @SuppressWarnings("unchecked")
    public static Condition in(
        String fieldName, Object attributeValue, Object... additionalValues) {
        Assert.notNull(attributeValue,
            "Attribute key of field " + fieldName + " must not be null"
        );
        if (attributeValue instanceof Collection<?> collection) {
            // Allow passing a collection directly
            return in(fieldName, (Collection<Object>) collection);
        }
        if (additionalValues == null) {
            return equal(fieldName, attributeValue);
        }
        var values = new ArrayList<>(additionalValues.length + 1);
        values.add(attributeValue);
        values.addAll(Arrays.asList(additionalValues));
        return in(fieldName, values);
    }

    /**
     * Creates an "in" condition for the specified field name and collection of values.
     *
     * @param fieldName the name of the field
     * @param values the collection of values, must not be null
     * @return the "in" condition
     */
    public static Condition in(String fieldName, Collection<Object> values) {
        Assert.notNull(values, "Values must not be null");
        if (values.size() == 1) {
            var value = values.iterator().next();
            return equal(fieldName, value);
        }
        return new InCondition(fieldName, values);
    }

    /**
     * Creates a "less than" condition for the specified field name and attribute value.
     *
     * @param fieldName the name of the field
     * @param attributeValue the attribute value, must not be null
     * @param inclusive whether the comparison is inclusive
     * @return the "less than" condition
     */
    public static Condition lessThan(String fieldName, Object attributeValue, boolean inclusive) {
        return new LessThanCondition(fieldName, attributeValue, inclusive);
    }

    /**
     * Creates a "less than" condition for the specified field name and attribute value.
     *
     * @param fieldName the name of the field
     * @param attributeValue the attribute value, must not be null. Which is not inclusive.
     * @return the "less than" condition
     */
    public static Condition lessThan(String fieldName, Object attributeValue) {
        return lessThan(fieldName, attributeValue, false);
    }

    /**
     * Creates a "not equal" condition for the specified field name and attribute value.
     *
     * @param fieldName the name of the field
     * @param attributeValue the attribute value, must not be null
     * @return the "not equal" condition
     */
    public static Condition notEqual(String fieldName, Object attributeValue) {
        return new NotEqualCondition(fieldName, attributeValue);
    }

    /**
     * Creates a "starts with" condition for the specified field name and prefix value.
     *
     * @param fieldName the name of the field
     * @param prefix the prefix value, must not be null
     * @return the "starts with" condition
     */
    public static Condition startsWith(String fieldName, String prefix) {
        return new StringStartsWithCondition(fieldName, prefix);
    }

    /**
     * Creates an "ends with" condition for the specified field name and suffix value.
     *
     * @param fieldName the name of the field
     * @param suffix the suffix value, must not be null
     * @return the "ends with" condition
     */
    public static Condition endsWith(String fieldName, String suffix) {
        return new StringEndsWithCondition(fieldName, suffix);
    }

    /**
     * Creates a "contains" condition for the specified field name and substring value.
     *
     * @param fieldName the name of the field
     * @param substring the substring value, must not be null
     * @return the "contains" condition
     */
    public static Condition contains(String fieldName, String substring) {
        return new StringContainsCondition(fieldName, substring);
    }

    /**
     * Creates a label condition that checks for the existence of a label with the specified key.
     *
     * @param labelKey the label key, must not be null
     * @return the label existence condition
     */
    public static LabelCondition labelExists(String labelKey) {
        return new LabelExistsCondition(labelKey);
    }

    /**
     * Creates a label condition that checks for equality of a label with the specified key and
     * value.
     *
     * @param labelKey the label key, must not be null
     * @param labelValue the label value, must not be null
     * @return the label equality condition
     */
    public static LabelCondition labelEqual(String labelKey, String labelValue) {
        return new LabelEqualsCondition(labelKey, labelValue);
    }

    /**
     * Creates a label condition that checks if a label with the specified key has a value within
     * the given collection of values.
     *
     * @param labelKey the label key, must not be null
     * @param labelValues the collection of label values, must not be null
     * @return the label "in" condition
     */
    public static LabelCondition labelIn(String labelKey, Collection<String> labelValues) {
        return new LabelInCondition(labelKey, labelValues);
    }

}
