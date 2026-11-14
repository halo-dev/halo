package run.halo.app.extension.index;

import java.time.Instant;

/**
 * Codec between index keys and their canonical string representation for snapshot persistence. Supports the key types
 * actually used by index specs; fails fast on unsupported types.
 *
 * @since 2.26.0
 */
final class IndexKeyCodec {

    private IndexKeyCodec() {}

    static String encode(Object key) {
        if (key instanceof Instant instant) {
            return instant.toString();
        }
        return key.toString();
    }

    @SuppressWarnings("unchecked")
    static <K extends Comparable<K>> K decode(Class<K> keyType, String text) {
        if (keyType == String.class) {
            return (K) text;
        }
        if (keyType == UnknownKey.class) {
            return (K) new UnknownKey(text);
        }
        if (keyType == Long.class) {
            return (K) Long.valueOf(text);
        }
        if (keyType == Integer.class) {
            return (K) Integer.valueOf(text);
        }
        if (keyType == Instant.class) {
            return (K) Instant.parse(text);
        }
        if (keyType == Boolean.class) {
            return (K) Boolean.valueOf(text);
        }
        if (keyType.isEnum()) {
            return (K) Enum.valueOf((Class<? extends Enum>) keyType.asSubclass(Enum.class), text);
        }
        throw new IllegalArgumentException("Unsupported index key type: " + keyType.getName());
    }
}
