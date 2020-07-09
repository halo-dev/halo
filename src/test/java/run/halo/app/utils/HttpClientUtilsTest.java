package run.halo.app.utils;

import cn.hutool.core.lang.Tuple;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class HttpClientUtilsTest {


    @Test
    void resolveHttpProxyTest() throws Exception {
        final Method resolveHttpProxy = HttpClientUtils.class.getDeclaredMethod("resolveHttpProxy", String.class);
        resolveHttpProxy.setAccessible(true);

        Tuple result = (Tuple) resolveHttpProxy.invoke(null, "http://127.0.0.1");
        assert result.get(0).equals("http://127.0.0.1");

        result = (Tuple) resolveHttpProxy.invoke(null, "https://127.0.0.1");
        assert result.get(0).equals("https://127.0.0.1");

        result = (Tuple) resolveHttpProxy.invoke(null, "https://127.0.0.1:123");
        assert result.get(0).equals("https://127.0.0.1:123");

        result = (Tuple) resolveHttpProxy.invoke(null, "u:p@https://127.0.0.1:123");
        assert result.get(0).equals("https://127.0.0.1:123");
        assert result.get(1).equals("u");
        assert result.get(2).equals("p");

    }

}
