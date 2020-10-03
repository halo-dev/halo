package run.halo.app.utils;

import cn.hutool.core.lang.Tuple;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpClientUtilsTest {

    @Test
    void resolveHttpProxyTest() throws Exception {
        final Method resolveHttpProxy = HttpClientUtils.class.getDeclaredMethod("resolveHttpProxy", String.class);
        resolveHttpProxy.setAccessible(true);

        Tuple result = (Tuple) resolveHttpProxy.invoke(null, "http://127.0.0.1");
        assertEquals(result.get(0), "http://127.0.0.1:80");

        result = (Tuple) resolveHttpProxy.invoke(null, "https://127.0.0.1");
        assertEquals(result.get(0), "https://127.0.0.1:443");

        result = (Tuple) resolveHttpProxy.invoke(null, "https://127.0.0.1:123");
        assertEquals(result.get(0), "https://127.0.0.1:123");

        result = (Tuple) resolveHttpProxy.invoke(null, "https://u:p@127.0.0.1:123");
        assertArrayEquals(result.getMembers(), new Object[] {"https://127.0.0.1:123", "u", "p"});

        result = (Tuple) resolveHttpProxy.invoke(null, "https://u@127.0.0.1");
        assertArrayEquals(result.getMembers(), new Object[] {"https://127.0.0.1:443", "u", null});
    }

}
