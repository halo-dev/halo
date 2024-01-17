package run.halo.app.security.authentication.twofactor.totp;

import static com.j256.twofactorauth.TimeBasedOneTimePasswordUtil.generateBase32Secret;
import static com.j256.twofactorauth.TimeBasedOneTimePasswordUtil.validateCurrentNumber;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;

@Slf4j
@Component
public class DefaultTotpAuthService implements TotpAuthService {

    private final BytesEncryptor encryptor;

    public DefaultTotpAuthService(HaloProperties haloProperties) {
        // init secret key
        var keysRoot = haloProperties.getWorkDir().resolve("keys");
        this.encryptor = loadOrCreateEncryptor(keysRoot);
    }

    private BytesEncryptor loadOrCreateEncryptor(Path keysRoot) {
        try {
            if (Files.notExists(keysRoot)) {
                Files.createDirectories(keysRoot);
            }
            var keyStorePath = keysRoot.resolve("halo.keystore");
            var password = "changeit".toCharArray();
            var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            if (Files.notExists(keyStorePath)) {
                keyStore.load(null, password);
            } else {
                try (var is = Files.newInputStream(keyStorePath, READ)) {
                    keyStore.load(is, password);
                }
            }

            var alias = "totp-secret-key";
            var entry = keyStore.getEntry(alias, new KeyStore.PasswordProtection(password));
            SecretKey secretKey = null;
            if (entry instanceof KeyStore.SecretKeyEntry secretKeyEntry) {
                if ("AES".equalsIgnoreCase(secretKeyEntry.getSecretKey().getAlgorithm())) {
                    secretKey = secretKeyEntry.getSecretKey();
                }
            }
            if (secretKey == null) {
                var generator = KeyGenerator.getInstance("AES");
                generator.init(128);
                secretKey = generator.generateKey();
                var secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
                keyStore.setEntry(alias, secretKeyEntry, new KeyStore.PasswordProtection(password));
                try (var os = Files.newOutputStream(keyStorePath, CREATE, APPEND)) {
                    keyStore.store(os, password);
                }
            }
            return new AesBytesEncryptor(secretKey,
                KeyGenerators.secureRandom(32),
                AesBytesEncryptor.CipherAlgorithm.GCM);
        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException
                 | UnrecoverableEntryException e) {
            throw new RuntimeException("Failed to initialize AesBytesEncryptor", e);
        }
    }

    @Override
    public boolean validateTotp(String rawSecret, int code) {
        try {
            return validateCurrentNumber(rawSecret, code, 10 * 1000);
        } catch (GeneralSecurityException e) {
            log.warn("Error occurred when validate TOTP code", e);
            return false;
        }
    }

    @Override
    public String generateTotpSecret() {
        return generateBase32Secret(32);
    }

    @Override
    public String encryptSecret(String rawSecret) {
        return new String(Hex.encode(encryptor.encrypt(rawSecret.getBytes())));
    }

    @Override
    public String decryptSecret(String encryptedSecret) {
        return new String(encryptor.decrypt(Hex.decode(encryptedSecret)));
    }
}
