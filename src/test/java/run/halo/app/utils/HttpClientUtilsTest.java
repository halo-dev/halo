package run.halo.app.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

public class HttpClientUtilsTest {

    @Test
    void resolveHttpProxyTest() throws Exception {
        final Method resolveHttpProxy =
            HttpClientUtils.class.getDeclaredMethod("resolveHttpProxy", String.class);
        resolveHttpProxy.setAccessible(true);

        Object[] result = (Object[]) resolveHttpProxy.invoke(null, "http://127.0.0.1");
        assertEquals("http://127.0.0.1:80", result[0]);

        result = (Object[]) resolveHttpProxy.invoke(null, "https://127.0.0.1");
        assertEquals("https://127.0.0.1:443", result[0]);

        result = (Object[]) resolveHttpProxy.invoke(null, "https://127.0.0.1:123");
        assertEquals("https://127.0.0.1:123", result[0]);

        result = (Object[]) resolveHttpProxy.invoke(null, "https://u:p@127.0.0.1:123");
        assertArrayEquals(new Object[] {"https://127.0.0.1:123", "u", "p"}, result);

        result = (Object[]) resolveHttpProxy.invoke(null, "https://u@127.0.0.1");
        assertArrayEquals(new Object[] {"https://127.0.0.1:443", "u", null}, result);
    }

}
