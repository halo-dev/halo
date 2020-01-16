package run.halo.app.utils;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.Test;

/**
 * BCrypt test.
 *
 * @author johnniang
 * @date 3/28/19
 */
public class BcryptTest {

    @Test
    public void cryptTest() {
        String cryptPassword = BCrypt.hashpw("opentest", BCrypt.gensalt());
        System.out.println("Crypt password: " + cryptPassword);
    }
}
