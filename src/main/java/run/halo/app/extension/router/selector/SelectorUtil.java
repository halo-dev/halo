package run.halo.app.extension.router.selector;

import java.util.List;
import java.util.function.Predicate;
import org.springframework.data.util.Predicates;
import org.springframework.web.server.ServerWebInputException;
import run.halo.app.extension.Extension;

public final class SelectorUtil {

    private SelectorUtil() {
    }

    public static <E extends Extension> Predicate<E> labelSelectorsToPredicate(
        List<String> labelSelectors) {
        if (labelSelectors == null) {
            labelSelectors = List.of();
        }

        final var labelPredicateConverter =
            new SelectorConverter().andThen(new LabelCriteriaPredicateConverter<E>());

        return labelSelectors.stream()
            .map(selector -> {
                var predicate = labelPredicateConverter.convert(selector);
                if (predicate == null) {
                    throw new ServerWebInputException("Invalid label selector: " + selector);
                }
                return predicate;
            })
            .reduce(Predicate::and)
            .orElse(Predicates.isTrue());
    }

    public static <E extends Extension> Predicate<E> fieldSelectorToPredicate(
        List<String> fieldSelectors) {
        if (fieldSelectors == null) {
            fieldSelectors = List.of();
        }

        final var fieldPredicateConverter =
            new SelectorConverter().andThen(new FieldCriteriaPredicateConverter<E>());

        return fieldSelectors.stream()
            .map(selector -> {
                var predicate = fieldPredicateConverter.convert(selector);
                if (predicate == null) {
                    throw new ServerWebInputException("Invalid field selector: " + selector);
                }
                return predicate;
            })
            .reduce(Predicate::and)
            .orElse(Predicates.isTrue());
    }

    public static <E extends Extension> Predicate<E> labelAndFieldSelectorToPredicate(
        List<String> labelSelectors, List<String> fieldSelectors) {
        return SelectorUtil.<E>labelSelectorsToPredicate(labelSelectors)
            .and(fieldSelectorToPredicate(fieldSelectors));
    }
}
