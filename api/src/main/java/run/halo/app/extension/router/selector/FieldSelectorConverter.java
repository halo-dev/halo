package run.halo.app.extension.router.selector;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

public class FieldSelectorConverter implements Converter<SelectorCriteria, SelectorMatcher> {

    @NonNull
    @Override
    public SelectorMatcher convert(@NonNull SelectorCriteria criteria) {
        var key = criteria.key();
        // compatible with old field selector
        if ("name".equals(key)) {
            key = "metadata.name";
        }
        switch (criteria.operator()) {
            case Equals -> {
                return EqualityMatcher.equal(key, getSingleValue(criteria));
            }
            case NotEquals -> {
                return EqualityMatcher.notEqual(key, getSingleValue(criteria));
            }
            // compatible with old field selector
            case IN -> {
                var valueArr =
                    defaultIfNull(criteria.values(), Set.<String>of()).toArray(new String[0]);
                return SetMatcher.in(key, valueArr);
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
