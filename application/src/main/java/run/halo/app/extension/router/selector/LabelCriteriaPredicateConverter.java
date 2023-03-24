package run.halo.app.extension.router.selector;

import java.util.function.Predicate;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import run.halo.app.extension.Extension;

public class LabelCriteriaPredicateConverter<E extends Extension>
    implements Converter<SelectorCriteria, Predicate<E>> {

    @Override
    @NonNull
    public Predicate<E> convert(SelectorCriteria criteria) {
        return ext -> {
            var labels = ext.getMetadata().getLabels();
            switch (criteria.operator()) {
                case Equals -> {
                    if (labels == null || !labels.containsKey(criteria.key())) {
                        return false;
                    }
                    return criteria.values().contains(labels.get(criteria.key()));
                }
                case NotEquals -> {
                    if (labels == null || !labels.containsKey(criteria.key())) {
                        return false;
                    }
                    return !criteria.values().contains(labels.get(criteria.key()));
                }
                case NotExist -> {
                    return labels == null || !labels.containsKey(criteria.key());
                }
                case Exist -> {
                    return labels != null && labels.containsKey(criteria.key());
                }
                default -> {
                    return false;
                }
            }
        };
    }
}
