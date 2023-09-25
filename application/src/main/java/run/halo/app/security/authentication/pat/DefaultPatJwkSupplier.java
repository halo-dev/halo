package run.halo.app.security.authentication.pat;

import static com.nimbusds.jose.jwk.KeyOperation.SIGN;
import static com.nimbusds.jose.jwk.KeyOperation.VERIFY;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;

@Slf4j
@Component
public class DefaultPatJwkSupplier implements PatJwkSupplier {

    private final RSAKey rsaKey;

    public DefaultPatJwkSupplier(HaloProperties haloProperties) throws JOSEException {
        var keyPair = getRsaKeyPairOrCreate(haloProperties);
        this.rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
            .privateKey(keyPair.getPrivate())
            .keyUse(KeyUse.SIGNATURE)
            .keyOperations(Set.of(SIGN, VERIFY))
            .keyIDFromThumbprint()
            .algorithm(JWSAlgorithm.RS256)
            .build();
    }

    private KeyPair getRsaKeyPairOrCreate(HaloProperties haloProperties) {
        var keysRoot = haloProperties.getWorkDir().resolve("keys");
        var privKeyPath = keysRoot.resolve("pat_id_rsa");
        var pubKeyPath = keysRoot.resolve("pat_id_rsa.pub");
        try {
            if (Files.exists(privKeyPath) && Files.exists(pubKeyPath)) {
                log.debug("Skip initializing RSA Keys for PAT due to existence.");

                var keyFactory = KeyFactory.getInstance("RSA");

                var privKeyBytes = Files.readAllBytes(privKeyPath);
                var privKeySpec = new PKCS8EncodedKeySpec(privKeyBytes);
                var privKey = keyFactory.generatePrivate(privKeySpec);

                var pubKeyBytes = Files.readAllBytes(pubKeyPath);
                var pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
                var pubKey = keyFactory.generatePublic(pubKeySpec);

                return new KeyPair(pubKey, privKey);
            }

            if (Files.notExists(keysRoot)) {
                Files.createDirectories(keysRoot);
            }
            Files.createFile(privKeyPath);
            Files.createFile(pubKeyPath);

            log.info("Generating RSA keys for PAT.");
            var rsaKey = new RSAKeyGenerator(4096).generate();
            var pubKey = rsaKey.toRSAPublicKey();
            var privKey = rsaKey.toRSAPrivateKey();
            Files.write(privKeyPath, privKey.getEncoded(), TRUNCATE_EXISTING);
            Files.write(pubKeyPath, pubKey.getEncoded(), TRUNCATE_EXISTING);
            log.info("Wrote RSA keys for PAT into {} and {}", privKeyPath, pubKeyPath);
            return new KeyPair(pubKey, privKey);
        } catch (JOSEException | IOException
                 | InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JWK getJwk() {
        return rsaKey;
    }
}
