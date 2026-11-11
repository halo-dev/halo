package run.halo.app.extension.router;

import java.util.Objects;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebInputException;
import run.halo.app.extension.Extension;
import run.halo.app.extension.JsonExtension;
import run.halo.app.extension.Scheme;

/**
 * Guards extension mutation endpoints against identity rewrite.
 *
 * <p>Authorization for these endpoints is derived from the URL path, so a request that changes {@code apiVersion},
 * {@code kind}, or {@code metadata.name} would otherwise be applied to a different extension than the one the caller
 * was authorized to modify.
 */
final class ExtensionIdentity {

    private ExtensionIdentity() {}

    static void requireUnchanged(Scheme scheme, String name, Extension extension) {
        if (!matches(scheme, name, extension)) {
            throw new ServerWebInputException(
                    "Cannot change apiVersion, kind, or metadata.name of " + scheme.groupVersionKind());
        }
    }

    static boolean matches(Scheme scheme, String name, Extension extension) {
        if (extension == null) {
            return false;
        }
        try {
            // JsonExtension getters coerce JSON scalars to strings. Identity fields must be strings.
            if (extension instanceof JsonExtension json) {
                var node = json.getInternal();
                if (!node.path("apiVersion").isTextual()
                        || !node.path("kind").isTextual()
                        || !node.path("metadata").path("name").isTextual()) {
                    return false;
                }
            }
            var metadata = extension.getMetadata();
            return metadata != null
                    && StringUtils.hasText(metadata.getName())
                    && Objects.equals(name, metadata.getName())
                    && Objects.equals(scheme.groupVersionKind().kind(), extension.getKind())
                    && Objects.equals(scheme.groupVersionKind().groupVersion().toString(), extension.getApiVersion());
        } catch (RuntimeException e) {
            // Malformed identity or metadata must be rejected before reaching the client.
            return false;
        }
    }
}
