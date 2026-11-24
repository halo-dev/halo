package run.halo.app.localization;

import reactor.core.publisher.Flux;

/**
 * Resolves enabled explicit descriptors and annotation declarations from currently registered Extension schemes.
 * Annotation declarations provide the base contract and one explicit descriptor may supplement complex fields,
 * collections and document behavior. Multiple explicit descriptors for one resource type remain ambiguous. Unregistered
 * plugin classes disappear from subsequent resolutions, without scanning the application classpath.
 *
 * @since 2.28.0
 */
public interface ResourceLocalizationDescriptors {
    /** Returns current declarations; consumers must reject ambiguous duplicate explicit descriptors. */
    Flux<ResourceLocalizationDescriptor> getDescriptors();
}
