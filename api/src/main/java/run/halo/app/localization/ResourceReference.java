package run.halo.app.localization;

import java.util.Objects;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;

/**
 * Identifies the original resource without exposing its mutable contents to a translation provider.
 *
 * <p>The group, version, kind and metadata name remain the same for every language. A translated title, slug or
 * permalink is not a resource identity and must not be used as the name. Translation providers use this reference with
 * the requested locale to find their stored translations.
 *
 * @param gvk versioned resource type
 * @param name nonblank {@code metadata.name} of the source resource
 * @since 2.28.0
 */
public record ResourceReference(GroupVersionKind gvk, String name) {
    /**
     * Creates a reference without reading or checking the existence of the resource.
     *
     * @throws NullPointerException if the resource type is null
     * @throws IllegalArgumentException if the metadata name is null or blank
     */
    public ResourceReference {
        Objects.requireNonNull(gvk);
        if (name == null) {
            throw new IllegalArgumentException("Resource name is required");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Resource name is required");
        }
    }

    /**
     * Creates a provider-safe identity from a source resource.
     *
     * @param source resource with initialized metadata and a nonblank metadata name
     * @return the versioned resource reference
     * @throws NullPointerException if the resource or its metadata is null
     * @throws IllegalArgumentException if its metadata name is null or blank
     */
    public static ResourceReference from(Extension source) {
        return new ResourceReference(
                source.groupVersionKind(), source.getMetadata().getName());
    }
}
