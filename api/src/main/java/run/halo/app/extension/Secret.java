package run.halo.app.extension;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Secret is a small piece of sensitive data which should be kept secret, such as a password,
 * a token, or a key.
 *
 * @author guqing
 * @see
 * <a href="https://github.com/kubernetes/kubernetes/blob/f33498a8256b455b677ad4d30440869318b84204/staging/src/k8s.io/api/core/v1/types.go">kebernetes Secret</a>
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "", version = "v1alpha1", kind = Secret.KIND, plural = "secrets", singular = "secret")
public class Secret extends AbstractExtension {
    public static final String KIND = "Secret";

    public static final String SECRET_TYPE_OPAQUE = "Opaque";

    public static final int MAX_SECRET_SIZE = 1024 * 1024;

    /**
     * Used to facilitate programmatic handling of secret data.
     * More info:
     * <a href="https://kubernetes.io/docs/concepts/configuration/secret/#secret-types">secret-types</a>
     */
    private String type;

    /**
     * <p>The total bytes of the values in
     * the Data field must be less than {@link #MAX_SECRET_SIZE} bytes.</p>
     * <p>{@code data} contains the secret data. Each key must consist of alphanumeric
     * characters, '-', '_' or '.'. The serialized form of the secret data is a
     * base64 encoded string, representing the arbitrary (possibly non-string)
     * data value here. Described in
     * <a href="https://tools.ietf.org/html/rfc4648#section-4">rfc4648#section-4</a>
     * </p>
     */
    private Map<String, byte[]> data;

    /**
     * {@code stringData} allows specifying non-binary secret data in string form.
     * It is provided as a write-only input field for convenience.
     * All keys and values are merged into the data field on write, overwriting any existing
     * values.
     * The stringData field is never output when reading from the API.
     */
    private Map<String, String> stringData;

}
