package run.halo.app.utils;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * URI test.
 *
 * @author johnniang
 * @date 3/26/19
 */
public class URITest {

    @Test
    public void createURITest() throws URISyntaxException {
        String homeDir = System.getProperty("user.home");
        URI uri = new URI(homeDir);
        System.out.println(uri);
    }
}
