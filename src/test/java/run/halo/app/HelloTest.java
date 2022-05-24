package run.halo.app;

import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import run.halo.app.identity.apitoken.PersonalAccessTokenUtils;
import run.halo.app.infra.utils.Base62;

/**
 * @author guqing
 * @since 2.0.0
 */
public class HelloTest {

    @Test
    public void test() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();
        String s = PersonalAccessTokenUtils.convertSecretKeyToString(secretKey);
        System.out.println("secretKey: " + s);
        String generate = PersonalAccessTokenUtils.generate(secretKey);
        String decodedPersonalAccessToken = Base62.decodeToString(generate);
        boolean b = PersonalAccessTokenUtils.verifyChecksum(generate, secretKey);
        System.out.println(b);
        System.out.println(decodedPersonalAccessToken);
    }
}
