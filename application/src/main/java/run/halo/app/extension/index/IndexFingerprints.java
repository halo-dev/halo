package run.halo.app.extension.index;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Computes structural fingerprints of index specs. The fingerprint covers name, single/multi kind, key type, unique,
 * nullable and the spec version; it intentionally does NOT cover the index function body, which is why
 * {@link ValueIndexSpec#getVersion()} exists.
 *
 * @since 2.26.0
 */
public final class IndexFingerprints {

    /** Fingerprint of the built-in label index, which has no user-facing spec. */
    public static final String LABEL_FINGERPRINT = "metadata.labels|label|v1";

    private IndexFingerprints() {}

    public static String fingerprint(ValueIndexSpec<?, ?> spec) {
        var kind = spec instanceof MultiValueIndexSpec<?, ?> ? "multi" : "single";
        var canonical = String.join(
                "|",
                spec.getName(),
                kind,
                spec.getKeyType().getName(),
                Boolean.toString(spec.isUnique()),
                Boolean.toString(spec.isNullable()),
                Integer.toString(spec.getVersion()));
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
