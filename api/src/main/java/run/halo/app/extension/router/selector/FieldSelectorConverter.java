package run.halo.app.extension.router.selector;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.index.query.Condition;
import run.halo.app.extension.index.query.Queries;

public class FieldSelectorConverter implements Converter<SelectorCriteria, Condition> {

    @NonNull
    @Override
    public Condition convert(@NonNull SelectorCriteria criteria) {
        var key = criteria.key();
        // compatible with old field selector
        if ("name".equals(key)) {
            key = "metadata.name";
        }
        switch (criteria.operator()) {
            case Equals -> {
                return Queries.equal(key, getSingleValue(criteria));
            }
            case NotEquals -> {
                return Queries.notEqual(key, getSingleValue(criteria));
            }
            // compatible with old field selector
            case IN -> {
                Set<String> valueArr = defaultIfNull(criteria.values(), Set.of());
                return Queries.in(key, valueArr);
            }
            default -> throw new IllegalArgumentException(
                "Unsupported operator: " + criteria.operator());
        }
    }

    String getSingleValue(SelectorCriteria criteria) {
        if (CollectionUtils.isEmpty(criteria.values())) {
            throw new IllegalArgumentException("No value present for label key: " + criteria.key());
        }
        return criteria.values().iterator().next();
    }
}
