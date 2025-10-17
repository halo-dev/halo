package run.halo.app.extension.index.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;

/**
 * Query factory utility class.
 *
 * @deprecated Use {@link Queries} instead.
 */
@Deprecated(since = "2.22.0", forRemoval = true)
@UtilityClass
public class QueryFactory {

    public static Query all() {
        return new EmptyCondition();
    }

    public static Query all(String fieldName) {
        return new AllCondition(fieldName);
    }

    public static Query isNull(String fieldName) {
        return new IsNullCondition(fieldName);
    }

    public static Query isNotNull(String fieldName) {
        return new IsNotNullCondition(fieldName);
    }

    public static Query notEqual(String fieldName, String attributeValue) {
        return Queries.notEqual(fieldName, attributeValue);
    }

    public static Query equal(String fieldName, String attributeValue) {
        return Queries.equal(fieldName, attributeValue);
    }

    public static Query lessThan(String fieldName, String attributeValue) {
        return Queries.lessThan(fieldName, attributeValue);
    }

    public static Query lessThanOrEqual(String fieldName, String attributeValue) {
        return Queries.lessThan(fieldName, attributeValue, true);
    }

    public static Query greaterThan(String fieldName, String attributeValue) {
        return Queries.greaterThan(fieldName, attributeValue);
    }

    public static Query greaterThanOrEqual(String fieldName, String attributeValue) {
        return Queries.greaterThan(fieldName, attributeValue, true);
    }

    public static Query in(String fieldName, String... attributeValues) {
        return Queries.in(fieldName, Set.of(attributeValues));
    }

    public static Query in(String fieldName, Collection<String> values) {
        var convertedValues = values.stream()
            .map(v -> (Object) v)
            .collect(Collectors.toSet());
        return Queries.in(fieldName, convertedValues);
    }

    public static Query and(Collection<Query> queries) {
        Assert.notEmpty(queries, "Queries must not be empty");
        if (queries.size() == 1) {
            return queries.iterator().next();
        }
        return queries.stream()
            .peek(query -> {
                if (!(query instanceof Condition)) {
                    throw new IllegalArgumentException(
                        "Only Condition instances are supported in AND operations");
                }
            })
            .map(query -> (Condition) query)
            .reduce(Condition::and)
            .orElseThrow(() -> new IllegalArgumentException("No Condition found in queries"));
    }

    public static And and(Query left, Query right) {
        Assert.isInstanceOf(Condition.class, left,
            "Only Condition instances are supported in AND operations");
        Assert.isInstanceOf(Condition.class, right,
            "Only Condition instances are supported in AND operations");
        return new And((Condition) left, (Condition) right);
    }

    public static Query and(Query left, Query right, Query... additionalQueries) {
        var queries = new ArrayList<Query>(2 + additionalQueries.length);
        queries.add(left);
        queries.add(right);
        Collections.addAll(queries, additionalQueries);
        return and(queries);
    }

    public static Query and(Query left, Query right, Collection<Query> additionalQueries) {
        var queries = new ArrayList<Query>(2 + additionalQueries.size());
        queries.add(left);
        queries.add(right);
        queries.addAll(additionalQueries);
        return and(queries);
    }

    private static Query or(Collection<Query> queries) {
        Assert.notEmpty(queries, "Queries must not be empty");
        if (queries.size() == 1) {
            return queries.iterator().next();
        }
        return queries.stream()
            .peek(query -> {
                if (!(query instanceof Condition)) {
                    throw new IllegalArgumentException(
                        "Only Condition instances are supported in OR operations");
                }
            })
            .map(query -> (Condition) query)
            .reduce(Condition::or)
            .orElseThrow(() -> new IllegalArgumentException("No Condition found in queries"));
    }

    public static Query or(Query left, Query right) {
        return or(List.of(left, right));
    }

    public static Query or(Query query1, Query query2, Query... additionalQueries) {
        var queries = new ArrayList<Query>(2 + additionalQueries.length);
        queries.add(query1);
        queries.add(query2);
        Collections.addAll(queries, additionalQueries);
        return or(queries);
    }

    public static Query or(Query query1, Query query2, Collection<Query> additionalQueries) {
        var queries = new ArrayList<Query>(2 + additionalQueries.size());
        queries.add(query1);
        queries.add(query2);
        queries.addAll(additionalQueries);
        return or(queries);
    }

    public static Query not(Query query) {
        Assert.isInstanceOf(Condition.class, query,
            "Only Condition instances are supported in NOT operations");
        return ((Condition) query).not();
    }

    public static Query betweenLowerExclusive(String fieldName, String lowerValue,
        String upperValue) {
        return Queries.between(fieldName, lowerValue, false, upperValue, true);
    }

    public static Query betweenUpperExclusive(String fieldName, String lowerValue,
        String upperValue) {
        return Queries.between(fieldName, lowerValue, true, upperValue, false);
    }

    public static Query betweenExclusive(String fieldName, String lowerValue,
        String upperValue) {
        return Queries.between(fieldName, lowerValue, false, upperValue, false);
    }

    public static Query between(String fieldName, String lowerValue, String upperValue) {
        return Queries.between(fieldName, lowerValue, true, upperValue, true);
    }

    public static Query startsWith(String fieldName, String value) {
        return Queries.startsWith(fieldName, value);
    }

    public static Query endsWith(String fieldName, String value) {
        return Queries.endsWith(fieldName, value);
    }

    public static Query contains(String fieldName, String value) {
        return Queries.contains(fieldName, value);
    }

}
