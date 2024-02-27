package run.halo.app.notification;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.NotificationTemplate;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;

/**
 * A default implementation of {@link ReasonNotificationTemplateSelector}.
 *
 * @author guqing
 * @since 2.10.0
 */
@Component
@RequiredArgsConstructor
public class ReasonNotificationTemplateSelectorImpl implements ReasonNotificationTemplateSelector {

    private final ReactiveExtensionClient client;

    @Override
    public Mono<NotificationTemplate> select(String reasonType, Locale locale) {
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(
            equal("spec.reasonSelector.reasonType", reasonType))
        );
        return client.listAll(NotificationTemplate.class, listOptions, Sort.unsorted())
            .collect(Collectors.groupingBy(
                getLanguageKey(),
                Collectors.maxBy(Comparator.comparing(t -> t.getMetadata().getCreationTimestamp()))
            ))
            .mapNotNull(map -> lookupTemplateByLocale(locale, map));
    }

    @Nullable
    static NotificationTemplate lookupTemplateByLocale(Locale locale,
        Map<String, Optional<NotificationTemplate>> map) {
        return LanguageUtils.computeLangFromLocale(locale).stream()
            // reverse order to ensure that the variant is the first element and the default
            // is the last element
            .sorted(Collections.reverseOrder())
            .map(key -> map.getOrDefault(key, Optional.empty()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElse(null);
    }

    @NonNull
    static Predicate<NotificationTemplate> matchReasonType(String reasonType) {
        return template -> template.getSpec().getReasonSelector().getReasonType()
            .equals(reasonType);
    }

    static Function<NotificationTemplate, String> getLanguageKey() {
        return template -> defaultIfBlank(template.getSpec().getReasonSelector().getLanguage(),
            "default");
    }
}
