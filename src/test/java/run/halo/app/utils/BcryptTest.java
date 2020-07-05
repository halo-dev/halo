package run.halo.app.utils;

import cn.hutool.crypto.digest.BCrypt;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * BCrypt test.
 *
 * @author johnniang
 * @date 3/28/19
 */
@Slf4j
class BcryptTest {

    @Test
    void cryptTest() {
        String cryptPassword = BCrypt.hashpw("opentest", BCrypt.gensalt());
        log.debug("Crypt password: [{}]", cryptPassword);
    }
}
