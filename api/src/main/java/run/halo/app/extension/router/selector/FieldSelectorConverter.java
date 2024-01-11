package run.halo.app.extension.router.selector;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.index.query.QueryFactory;

public class FieldSelectorConverter implements Converter<SelectorCriteria, Query> {

    @NonNull
    @Override
    public Query convert(@NonNull SelectorCriteria criteria) {
        var key = criteria.key();
        // compatible with old field selector
        if ("name".equals(key)) {
            key = "metadata.name";
        }
        switch (criteria.operator()) {
            case Equals -> {
                return QueryFactory.equal(key, getSingleValue(criteria));
            }
            case NotEquals -> {
                return QueryFactory.notEqual(key, getSingleValue(criteria));
            }
            // compatible with old field selector
            case IN -> {
                var valueArr = defaultIfNull(criteria.values(), Set.<String>of());
                return QueryFactory.in(key, valueArr);
            }
            default -> throw new IllegalArgumentException(
                "Unsupported operator: " + criteria.operator());
        }
    }

    String getSingleValue(SelectorCriteria criteria) {
        if (CollectionUtils.isEmpty(criteria.values())) {
            return null;
        }
        return criteria.values().iterator().next();
    }
}
