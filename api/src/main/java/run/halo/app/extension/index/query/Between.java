package run.halo.app.extension.index.query;

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
        return indexView.between(fieldName, lowerValue, lowerInclusive, upperValue, upperInclusive);
    }

    @Override
    public String toString() {
        return fieldName + " BETWEEN " + (lowerInclusive ? "[" : "(") + lowerValue + ", "
            + upperValue + (upperInclusive ? "]" : ")");
    }
}
