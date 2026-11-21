package run.halo.app.localization;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.context.ContextView;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.event.SchemeAddedEvent;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/**
 * Resolves declarations from enabled plugins and current schemes on each subscription. Annotation declarations form the
 * base contract; one explicit descriptor supplies complex fields, collections and document behavior. Only annotation
 * compilation is cached, using class-owned entries which do not prevent plugin classloader disposal.
 *
 * @since 2.28.0
 */
@Component
public class DefaultResourceLocalizationDescriptors implements ResourceLocalizationDescriptors {
    private final ExtensionGetter extensionGetter;
    private final SchemeManager schemes;
    private final LocalizableAnnotations annotations;
    private final ResourceLocalizationDescriptorLocalizer localizer;

    @Autowired
    public DefaultResourceLocalizationDescriptors(
            ExtensionGetter extensionGetter,
            SchemeManager schemes,
            ObjectMapper mapper,
            ResourceLocalizationDescriptorLocalizer localizer) {
        this.extensionGetter = extensionGetter;
        this.schemes = schemes;
        this.annotations = new LocalizableAnnotations(mapper);
        this.localizer = localizer;
    }

    DefaultResourceLocalizationDescriptors(
            ExtensionGetter extensionGetter, SchemeManager schemes, ObjectMapper mapper) {
        this(extensionGetter, schemes, mapper, ResourceLocalizationDescriptorLocalizer.identity());
    }

    /** Fails scheme registration before an invalid annotation declaration can become publicly readable. */
    @EventListener
    public void onSchemeAdded(SchemeAddedEvent event) {
        var type = event.getScheme().type();
        if (annotations.fields(type).isEmpty()) {
            return;
        }
        annotations.descriptor(type);
    }

    @Override
    public Flux<ResourceLocalizationDescriptor> getDescriptors() {
        return Flux.deferContextual(context -> descriptors(locale(context)));
    }

    private Flux<ResourceLocalizationDescriptor> descriptors(Locale locale) {
        return Flux.defer(() -> extensionGetter
                .getEnabledExtensions(ResourceLocalizationDescriptor.class)
                .collectList()
                .flatMapMany(explicit -> mergeDescriptors(explicit, locale)));
    }

    private Flux<ResourceLocalizationDescriptor> mergeDescriptors(
            List<ResourceLocalizationDescriptor> explicit, Locale locale) {
        var declarations = annotationDescriptors(locale);
        var explicitByKind = explicit.stream()
                .collect(Collectors.groupingBy(
                        ResourceLocalizationDescriptor::supports, LinkedHashMap::new, Collectors.toList()));
        var result = new ArrayList<ResourceLocalizationDescriptor>();
        explicitByKind.forEach((kind, descriptors) -> addExplicit(result, declarations.get(kind), descriptors, locale));
        declarations.forEach((kind, descriptor) -> {
            if (!explicitByKind.containsKey(kind)) {
                result.add(descriptor);
            }
        });
        return Flux.fromIterable(result);
    }

    private void addExplicit(
            List<ResourceLocalizationDescriptor> result,
            ResourceLocalizationDescriptor declarations,
            List<ResourceLocalizationDescriptor> explicit,
            Locale locale) {
        if (explicit.size() != 1 || declarations == null) {
            explicit.forEach(descriptor -> result.add(localizer.localize(descriptor, descriptor.getClass(), locale)));
            return;
        }
        var descriptor =
                localizer.localize(explicit.getFirst(), explicit.getFirst().getClass(), locale);
        CombinedResourceLocalizationDescriptor.combine(declarations, descriptor).ifPresentOrElse(result::add, () -> {
            result.add(declarations);
            result.add(descriptor);
        });
    }

    private Map<GroupVersionKind, ResourceLocalizationDescriptor> annotationDescriptors(Locale locale) {
        var result = new LinkedHashMap<GroupVersionKind, ResourceLocalizationDescriptor>();
        for (var scheme : schemes.schemes()) {
            if (annotations.fields(scheme.type()).isEmpty()) {
                continue;
            }
            var descriptor = annotations.descriptor(scheme.type());
            result.put(scheme.groupVersionKind(), localizer.localize(descriptor, scheme.type(), locale));
        }
        return result;
    }

    private static Locale locale(ContextView context) {
        return context.getOrDefault(Locale.class, LocaleContextHolder.getLocale());
    }
}
