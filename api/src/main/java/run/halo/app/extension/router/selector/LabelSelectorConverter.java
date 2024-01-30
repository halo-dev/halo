package run.halo.app.extension.router.selector;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

public class LabelSelectorConverter implements Converter<SelectorCriteria, SelectorMatcher> {

    @NonNull
    @Override
    public SelectorMatcher convert(@NonNull SelectorCriteria criteria) {
        switch (criteria.operator()) {
            case Equals -> {
                return EqualityMatcher.equal(criteria.key(), getSingleValue(criteria));
            }
            case NotEquals -> {
                return EqualityMatcher.notEqual(criteria.key(), getSingleValue(criteria));
            }
            case NotExist -> {
                return SetMatcher.notExists(criteria.key());
            }
            case Exist -> {
                return SetMatcher.exists(criteria.key());
            }
            case IN -> {
                var valueArr =
                    defaultIfNull(criteria.values(), Set.<String>of()).toArray(new String[0]);
                return SetMatcher.in(criteria.key(), valueArr);
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
