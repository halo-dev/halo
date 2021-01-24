package run.halo.app.service.support;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author johnniang
 * @date 19-4-19
 */
@Slf4j
class HaloMediaTypeTest {

    @Test
    void gitUrlCheckTest() throws URISyntaxException {
        String git = "https://github.com/halo-dev/halo.git";

        URI uri = new URI(git);

        log.debug(uri.toString());

        git = "ssh://git@github.com:halo-dev/halo.git";

        uri = new URI(git);

        log.debug(uri.toString());
    }
}