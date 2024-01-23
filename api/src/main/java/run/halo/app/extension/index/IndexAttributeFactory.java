package run.halo.app.extension.index;

import java.util.Set;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import run.halo.app.extension.Extension;

@UtilityClass
public class IndexAttributeFactory {

    public static <E extends Extension> IndexAttribute simpleAttribute(Class<E> type,
        Function<E, String> valueFunc) {
        return new FunctionalIndexAttribute<>(type, valueFunc);
    }

    public static <E extends Extension> IndexAttribute multiValueAttribute(Class<E> type,
        Function<E, Set<String>> valueFunc) {
        return new FunctionalMultiValueIndexAttribute<>(type, valueFunc);
    }
}
