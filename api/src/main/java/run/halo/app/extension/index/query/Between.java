package run.halo.app.extension.index.query;

import com.google.common.collect.Sets;
import java.util.NavigableSet;

public class Between extends SimpleQuery {
    private final String lowerValue;
    private final boolean lowerInclusive;
    private final String upperValue;
    private final boolean upperInclusive;

    public Between(String fieldName, String lowerValue, boolean lowerInclusive,
        String upperValue, boolean upperInclusive) {
        // value and isFieldRef are not used in Between
        super(fieldName, null, false);
        this.lowerValue = lowerValue;
        this.lowerInclusive = lowerInclusive;
        this.upperValue = upperValue;
        this.upperInclusive = upperInclusive;
    }


    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        NavigableSet<String> allValues = indexView.getAllValuesForField(fieldName);
        // get all values in the specified range
        var subSet = allValues.subSet(lowerValue, lowerInclusive, upperValue, upperInclusive);

        var resultSet = Sets.<String>newTreeSet();
        for (String val : subSet) {
            resultSet.addAll(indexView.getIdsForFieldValue(fieldName, val));
        }
        return resultSet;
    }
}
