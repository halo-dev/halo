package run.halo.app.localization;

import java.util.Objects;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;

/**
 * Identifies the original resource without exposing its mutable contents to a translation provider.
 *
 * <p>Extension resources use their group, version, kind and metadata name. Non-Extension resources use an explicit
 * owner namespace, resource type and stable ID without inventing a GVK. These identities remain the same in every
 * language. A translated title, slug or permalink must not be used as the identity. Translation providers combine this
 * reference with the requested locale to find stored translations.
 *
 * @param gvk versioned Extension type, or null for an external resource
 * @param name nonblank stable source identifier; {@code metadata.name} for Extensions
 * @param namespace external owner namespace, or null for Extensions
 * @param type external resource type, or null for Extensions
 * @since 2.28.0
 */
public record ResourceReference(GroupVersionKind gvk, String name, String namespace, String type) {
    /** Identifies an Extension by its registered type and metadata name. */
    public ResourceReference(GroupVersionKind gvk, String name) {
        this(Objects.requireNonNull(gvk), name, null, null);
    }

    /**
     * Identifies a resource stored outside Extension storage. Namespace and type are stable owner-defined identifiers,
     * not a generated Extension GVK. Include the tenant or data-source dimension in these identifiers or in the ID when
     * it affects uniqueness.
     */
    public static ResourceReference external(String namespace, String type, String id) {
        return new ResourceReference(null, id, namespace, type);
    }

    /** Returns whether this identity belongs to Extension storage. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isExtension() {
        return gvk != null;
    }
    /**
     * Creates a reference without reading or checking the existence of the resource.
     *
     * @throws IllegalArgumentException if the identity is incomplete or combines Extension and external components
     */
    public ResourceReference {
        if (gvk == null) {
            requireIdentifier(namespace, "Resource namespace");
            requireIdentifier(type, "Resource type");
        } else if (namespace != null || type != null) {
            throw new IllegalArgumentException("Extension identities cannot have an external namespace or type");
        }
        if (name == null) {
            throw new IllegalArgumentException("Resource name is required");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Resource name is required");
        }
    }

    private static void requireIdentifier(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
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
        Objects.requireNonNull(source, "Source resource is required");
        var metadata = Objects.requireNonNull(source.getMetadata(), "Source metadata is required");
        return new ResourceReference(source.groupVersionKind(), metadata.getName());
    }
}
