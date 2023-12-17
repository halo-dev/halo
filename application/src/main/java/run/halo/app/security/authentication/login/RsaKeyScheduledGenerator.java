package run.halo.app.security.authentication.login;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * RsaKeyScheduledGenerator is responsible for periodically generating RSA key pair.
 *
 * @author johnniang
 */
@Slf4j
public class RsaKeyScheduledGenerator {

    private final CryptoService cryptoService;

    public RsaKeyScheduledGenerator(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.DAYS)
    void scheduleGeneration() {
        cryptoService.generateKeys().block();
    }
}
