package run.halo.app.extension.router.selector;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.index.query.LabelCondition;
import run.halo.app.extension.index.query.Queries;

public class LabelSelectorConverter implements Converter<SelectorCriteria, LabelCondition> {

    @NonNull
    @Override
    public LabelCondition convert(@NonNull SelectorCriteria criteria) {
        switch (criteria.operator()) {
            case Equals -> {
                return Queries.labelEqual(criteria.key(), getSingleValue(criteria));
            }
            case NotEquals -> {
                return Queries.labelEqual(criteria.key(), getSingleValue(criteria)).not();
            }
            case NotExist -> {
                return Queries.labelExists(criteria.key()).not();
            }
            case Exist -> {
                return Queries.labelExists(criteria.key());
            }
            case IN -> {
                return Queries.labelIn(criteria.key(), defaultIfNull(criteria.values(), Set.of()));
            }
            default ->
                throw new IllegalArgumentException("Unsupported operator: " + criteria.operator());
        }
    }

    String getSingleValue(SelectorCriteria criteria) {
        if (CollectionUtils.isEmpty(criteria.values())) {
            throw new IllegalArgumentException("No value present for label key: " + criteria.key());
        }
        return criteria.values().iterator().next();
    }
}
