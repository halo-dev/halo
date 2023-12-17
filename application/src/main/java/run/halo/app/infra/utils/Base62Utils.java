package run.halo.app.infra.utils;

import io.seruco.encoding.base62.Base62;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>Base62 tool class, which provides the encoding and decoding scheme of base62.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class Base62Utils {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final Base62 INSTANCE = Base62.createInstance();

    public static String encode(String source) {
        return encode(source, DEFAULT_CHARSET);
    }

    /**
     * Base62 encode.
     *
     * @param source the encoded base62 string
     * @param charset the charset default is utf_8
     * @return encoded string by base62
     */
    public static String encode(String source, Charset charset) {
        return encode(StringUtils.getBytes(source, charset));
    }

    public static String encode(byte[] source) {
        return new String(INSTANCE.encode(source));
    }

    /**
     * Base62 decode.
     *
     * @param base62Str the Base62 decoded string
     * @return decoded bytes
     */
    public static byte[] decode(String base62Str) {
        return decode(StringUtils.getBytes(base62Str, DEFAULT_CHARSET));
    }

    public static byte[] decode(byte[] base62bytes) {
        return INSTANCE.decode(base62bytes);
    }

    public static String decodeToString(String source) {
        return decodeToString(source, DEFAULT_CHARSET);
    }

    public static String decodeToString(String source, Charset charset) {
        return StringUtils.toEncodedString(decode(source), charset);
    }
}
