package run.halo.app.extension.router.selector;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Slf4j
public class SelectorConverter implements Converter<String, SelectorCriteria> {

    @Override
    @Nullable
    public SelectorCriteria convert(@Nullable String selector) {
        return Arrays.stream(Operator.values())
            .sorted(Comparator.comparing(Operator::getOrder))
            .map(operator -> {
                log.debug("Resolving selector: {} with operator: {}", selector, operator);
                return operator.convert(selector);
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

}
