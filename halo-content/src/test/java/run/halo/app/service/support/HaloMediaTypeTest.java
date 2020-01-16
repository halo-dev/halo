package run.halo.app.service.support;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author johnniang
 * @date 19-4-19
 */
@Slf4j
public class HaloMediaTypeTest {

    @Test
    public void gitUrlCheckTest() throws URISyntaxException {
        String git = "https://github.com/halo-dev/halo.git";

        URI uri = new URI(git);

        log.debug(uri.toString());

        git = "ssh://git@github.com:halo-dev/halo.git";

        uri = new URI(git);

        log.debug(uri.toString());
    }
}