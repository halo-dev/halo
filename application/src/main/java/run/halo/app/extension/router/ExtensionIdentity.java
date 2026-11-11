package run.halo.app.extension.router;

import java.util.Objects;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebInputException;
import run.halo.app.extension.Extension;
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
        var metadata = extension.getMetadata();
        if (metadata == null || !StringUtils.hasText(metadata.getName()) || !Objects.equals(name, metadata.getName())) {
            return false;
        }
        try {
            return Objects.equals(scheme.groupVersionKind(), extension.groupVersionKind());
        } catch (RuntimeException e) {
            // Missing or invalid apiVersion/kind should be treated as an identity change.
            return false;
        }
    }
}
