package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * Two-Factor Auth Test
 *
 * @author Mu_Wooo
 * @date 2020-04-03 10:10 上午
 */
@Slf4j
public class TwoFactorAuthUtilsTest {

    @Test
    public void checkTFACodeTest() {
        // generate new key
        final String key = TwoFactorAuthUtils.generateTFAKey();
        // generate url
        final String totpUrl = TwoFactorAuthUtils.generateOtpAuthUrl("UnitTest", key);
        log.debug("generate key: {}, totpUrl: {}", key, totpUrl);
        // generate TFA code
        final String authCode = TwoFactorAuthUtils.generateTFACode(key);
        log.debug("TFA code: {}", authCode);
        // validate TFA code
        TwoFactorAuthUtils.validateTFACode(key, authCode);
        log.debug("Success!");
    }

}
