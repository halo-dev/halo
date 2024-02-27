package run.halo.app.extension.index.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;

@UtilityClass
public class QueryFactory {

    public static Query all() {
        return new All("metadata.name");
    }

    public static Query all(String fieldName) {
        return new All(fieldName);
    }

    public static Query isNull(String fieldName) {
        return new IsNull(fieldName);
    }

    public static Query isNotNull(String fieldName) {
        return new IsNotNull(fieldName);
    }

    /**
     * Create a {@link NotEqual} for the given {@code fieldName} and {@code attributeValue}.
     */
    public static Query notEqual(String fieldName, String attributeValue) {
        if (attributeValue == null) {
            return new IsNotNull(fieldName);
        }
        return new NotEqual(fieldName, attributeValue);
    }

    public static Query notEqualOtherField(String fieldName, String otherFieldName) {
        return new NotEqual(fieldName, otherFieldName, true);
    }

    /**
     * Create a {@link EqualQuery} for the given {@code fieldName} and {@code attributeValue}.
     */
    public static Query equal(String fieldName, String attributeValue) {
        if (attributeValue == null) {
            return new IsNull(fieldName);
        }
        return new EqualQuery(fieldName, attributeValue);
    }

    public static Query equalOtherField(String fieldName, String otherFieldName) {
        return new EqualQuery(fieldName, otherFieldName, true);
    }

    public static Query lessThanOtherField(String fieldName, String otherFieldName) {
        return new LessThanQuery(fieldName, otherFieldName, false, true);
    }

    public static Query lessThanOrEqualOtherField(String fieldName, String otherFieldName) {
        return new LessThanQuery(fieldName, otherFieldName, true, true);
    }

    public static Query lessThan(String fieldName, String attributeValue) {
        return new LessThanQuery(fieldName, attributeValue, false);
    }

    public static Query lessThanOrEqual(String fieldName, String attributeValue) {
        return new LessThanQuery(fieldName, attributeValue, true);
    }

    public static Query greaterThan(String fieldName, String attributeValue) {
        return new GreaterThanQuery(fieldName, attributeValue, false);
    }

    public static Query greaterThanOrEqual(String fieldName, String attributeValue) {
        return new GreaterThanQuery(fieldName, attributeValue, true);
    }

    public static Query greaterThanOtherField(String fieldName, String otherFieldName) {
        return new GreaterThanQuery(fieldName, otherFieldName, false, true);
    }

    public static Query greaterThanOrEqualOtherField(String fieldName,
        String otherFieldName) {
        return new GreaterThanQuery(fieldName, otherFieldName, true, true);
    }

    public static Query in(String fieldName, String... attributeValues) {
        return in(fieldName, Set.of(attributeValues));
    }

    /**
     * Create an {@link InQuery} for the given {@code fieldName} and {@code values}.
     */
    public static Query in(String fieldName, Collection<String> values) {
        Assert.notNull(values, "Values must not be null");
        if (values.size() == 1) {
            String singleValue = values.iterator().next();
            return equal(fieldName, singleValue);
        }
        // Copy the values into a Set if necessary...
        var valueSet = (values instanceof Set ? (Set<String>) values
            : new HashSet<>(values));
        return new InQuery(fieldName, valueSet);
    }

    /**
     * Create an {@link And} for the given {@link Query}s.
     */
    public static Query and(Collection<Query> queries) {
        Assert.notEmpty(queries, "Queries must not be empty");
        if (queries.size() == 1) {
            return queries.iterator().next();
        }
        return new And(queries);
    }

    public static And and(Query query1, Query query2) {
        Collection<Query> queries = Arrays.asList(query1, query2);
        return new And(queries);
    }

    /**
     * Create an {@link And} for the given {@link Query}s.
     */
    public static Query and(Query query1, Query query2, Query... additionalQueries) {
        var queries = new ArrayList<Query>(2 + additionalQueries.length);
        queries.add(query1);
        queries.add(query2);
        Collections.addAll(queries, additionalQueries);
        return new And(queries);
    }

    /**
     * Create an {@link And} for the given {@link Query}s.
     */
    public static Query and(Query query1, Query query2, Collection<Query> additionalQueries) {
        var queries = new ArrayList<Query>(2 + additionalQueries.size());
        queries.add(query1);
        queries.add(query2);
        queries.addAll(additionalQueries);
        return new And(queries);
    }

    public static Query or(Query query1, Query query2) {
        Collection<Query> queries = Arrays.asList(query1, query2);
        return new Or(queries);
    }

    /**
     * Create an {@link Or} for the given {@link Query}s.
     */
    public static Query or(Query query1, Query query2, Query... additionalQueries) {
        var queries = new ArrayList<Query>(2 + additionalQueries.length);
        queries.add(query1);
        queries.add(query2);
        Collections.addAll(queries, additionalQueries);
        return new Or(queries);
    }

    /**
     * Create an {@link Or} for the given {@link Query}s.
     */
    public static Query or(Query query1, Query query2, Collection<Query> additionalQueries) {
        var queries = new ArrayList<Query>(2 + additionalQueries.size());
        queries.add(query1);
        queries.add(query2);
        queries.addAll(additionalQueries);
        return new Or(queries);
    }

    public static Query not(Query query) {
        return new Not(query);
    }

    public static Query betweenLowerExclusive(String fieldName, String lowerValue,
        String upperValue) {
        return new Between(fieldName, lowerValue, false, upperValue, true);
    }

    public static Query betweenUpperExclusive(String fieldName, String lowerValue,
        String upperValue) {
        return new Between(fieldName, lowerValue, true, upperValue, false);
    }

    public static Query betweenExclusive(String fieldName, String lowerValue,
        String upperValue) {
        return new Between(fieldName, lowerValue, false, upperValue, false);
    }

    public static Query between(String fieldName, String lowerValue, String upperValue) {
        return new Between(fieldName, lowerValue, true, upperValue, true);
    }

    public static Query startsWith(String fieldName, String value) {
        return new StringStartsWith(fieldName, value);
    }

    public static Query endsWith(String fieldName, String value) {
        return new StringEndsWith(fieldName, value);
    }

    public static Query contains(String fieldName, String value) {
        return new StringContains(fieldName, value);
    }

    /**
     * Get all the field names used in the given query.
     *
     * @param query the query
     * @return the field names used in the given query
     */
    public static List<String> getFieldNamesUsedInQuery(Query query) {
        List<String> fieldNames = new ArrayList<>();

        if (query instanceof SimpleQuery simpleQuery) {
            if (simpleQuery.isFieldRef()) {
                fieldNames.add(simpleQuery.getValue());
            }
            fieldNames.add(simpleQuery.getFieldName());
        } else if (query instanceof LogicalQuery logicalQuery) {
            for (Query childQuery : logicalQuery.getChildQueries()) {
                fieldNames.addAll(getFieldNamesUsedInQuery(childQuery));
            }
        }
        return fieldNames;
    }
}
