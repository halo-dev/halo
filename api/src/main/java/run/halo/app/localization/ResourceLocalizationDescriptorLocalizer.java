package run.halo.app.localization;

import java.util.Locale;

/**
 * Resolves presentation labels declared by resource localization descriptors.
 *
 * <p>Descriptors retain message codes and semantic keys. This service replaces only labels, groups and display names
 * for the requested language; field locations, policies, identities and document bindings remain unchanged. A missing
 * message falls back to the declared value, so descriptors that use literal labels continue to work.
 *
 * @since 2.28.0
 */
public interface ResourceLocalizationDescriptorLocalizer {
    /**
     * Resolves an Extension descriptor for the requested language.
     *
     * @param descriptor source declaration
     * @param owner owning Core or plugin class used to locate the appropriate message bundle
     * @param locale Console language
     * @return a descriptor whose presentation labels are localized
     */
    ResourceLocalizationDescriptor localize(ResourceLocalizationDescriptor descriptor, Class<?> owner, Locale locale);

    /**
     * Resolves a non-Extension source descriptor for the requested language.
     *
     * @param descriptor source declaration
     * @param owner owning plugin class used to locate the appropriate message bundle
     * @param locale Console language
     * @return a descriptor whose presentation labels are localized
     */
    <T> LocalizableResourceDescriptor<T> localize(
            LocalizableResourceDescriptor<T> descriptor, Class<?> owner, Locale locale);

    /**
     * Resolves one standalone catalog label using the owner's message source.
     *
     * @param value message code or literal label
     * @param owner owning Core or plugin class
     * @param locale requested Console language
     * @return the resolved label, or the original value when no message exists
     */
    default String localizeLabel(String value, Class<?> owner, Locale locale) {
        return value;
    }

    /** Returns declarations unchanged. */
    static ResourceLocalizationDescriptorLocalizer identity() {
        return new ResourceLocalizationDescriptorLocalizer() {
            @Override
            public ResourceLocalizationDescriptor localize(
                    ResourceLocalizationDescriptor descriptor, Class<?> owner, Locale locale) {
                return descriptor;
            }

            @Override
            public <T> LocalizableResourceDescriptor<T> localize(
                    LocalizableResourceDescriptor<T> descriptor, Class<?> owner, Locale locale) {
                return descriptor;
            }

            @Override
            public String localizeLabel(String value, Class<?> owner, Locale locale) {
                return value;
            }
        };
    }
}
