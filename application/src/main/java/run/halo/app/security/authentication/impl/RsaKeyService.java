package run.halo.app.security.authentication.impl;

import static com.nimbusds.jose.jwk.KeyOperation.SIGN;
import static com.nimbusds.jose.jwk.KeyOperation.VERIFY;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Set;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.crypto.codec.Hex;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.security.authentication.CryptoService;
import run.halo.app.security.authentication.login.InvalidEncryptedMessageException;

@Slf4j
public class RsaKeyService implements CryptoService, InitializingBean {

    public static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    public static final String ALGORITHM = "RSA";

    private final Path keysRoot;

    private KeyPair keyPair;

    private String keyId;

    private JWK jwk;

    public RsaKeyService(Path dir) {
        this.keysRoot = dir;
    }

    @Override
    public void afterPropertiesSet() throws JOSEException {
        this.keyPair = this.getRsaKeyPairOrCreate();
        this.keyId = sha256(keyPair.getPrivate().getEncoded());
        this.jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey(keyPair.getPrivate())
                .keyUse(KeyUse.SIGNATURE)
                .keyOperations(Set.of(SIGN, VERIFY))
                .keyIDFromThumbprint()
                .algorithm(JWSAlgorithm.RS256)
                .build();
    }

    private KeyPair getRsaKeyPairOrCreate() {
        var privKeyPath = keysRoot.resolve("pat_id_rsa");
        var pubKeyPath = keysRoot.resolve("pat_id_rsa.pub");
        try {
            var keyFactory = KeyFactory.getInstance(ALGORITHM);
            var privKeyBytes = readKeyFileIfPresent(privKeyPath);
            var pubKeyBytes = readKeyFileIfPresent(pubKeyPath);
            if (privKeyBytes == null && pubKeyBytes != null) {
                log.warn(
                        "RSA private key is missing at {}. Generating a new key pair will invalidate "
                                + "JWTs signed with the previous key.",
                        privKeyPath);
            }
            RSAPrivateCrtKey existingPrivateKey = null;
            if (privKeyBytes != null) {
                try {
                    existingPrivateKey =
                            (RSAPrivateCrtKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));
                } catch (InvalidKeySpecException e) {
                    log.warn(
                            "Invalid RSA private key at {}. Generating a new key pair will invalidate "
                                    + "JWTs signed with the previous key.",
                            privKeyPath);
                }
            }
            if (existingPrivateKey != null) {
                var pubKey = keyFactory.generatePublic(
                        new RSAPublicKeySpec(existingPrivateKey.getModulus(), existingPrivateKey.getPublicExponent()));
                if (!Arrays.equals(pubKey.getEncoded(), pubKeyBytes)) {
                    log.warn("Restoring RSA public key at {} from the existing private key.", pubKeyPath);
                    writeKeyFileAtomically(pubKeyPath, pubKey.getEncoded());
                }
                return new KeyPair(pubKey, existingPrivateKey);
            }

            log.info("Generating RSA keys for PAT.");
            var rsaKey = new RSAKeyGenerator(4096).generate();
            var pubKey = rsaKey.toRSAPublicKey();
            var privKey = rsaKey.toRSAPrivateKey();
            Files.createDirectories(keysRoot);
            // Persist the private key first so an interrupted write can recover the public key.
            writeKeyFileAtomically(privKeyPath, privKey.getEncoded());
            writeKeyFileAtomically(pubKeyPath, pubKey.getEncoded());
            log.info("Wrote RSA keys for PAT into {} and {}", privKeyPath, pubKeyPath);
            return new KeyPair(pubKey, privKey);
        } catch (JOSEException | IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate or read RSA key pair", e);
        }
    }

    private static byte[] readKeyFileIfPresent(Path path) throws IOException {
        try {
            return Files.readAllBytes(path);
        } catch (NoSuchFileException e) {
            return null;
        }
    }

    private static void writeKeyFileAtomically(Path target, byte[] keyBytes) throws IOException {
        var tempPath =
                Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
        try {
            Files.write(tempPath, keyBytes);
            try {
                Files.move(tempPath, target, ATOMIC_MOVE, REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempPath, target, REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    @Override
    public Mono<byte[]> decrypt(byte[] encryptedMessage) {
        return Mono.just(this.keyPair)
                .map(KeyPair::getPrivate)
                .flatMap(privateKey -> {
                    try {
                        var cipher = Cipher.getInstance(TRANSFORMATION);
                        cipher.init(Cipher.DECRYPT_MODE, privateKey);
                        return Mono.just(cipher.doFinal(encryptedMessage));
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
                        return Mono.error(
                                new RuntimeException("Failed to read private key or the key was invalid.", e));
                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                        return Mono.error(new InvalidEncryptedMessageException("Invalid encrypted message."));
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<byte[]> readPublicKey() {
        return Mono.just(keyPair).map(KeyPair::getPublic).map(PublicKey::getEncoded);
    }

    @Override
    public String getKeyId() {
        return this.keyId;
    }

    @Override
    public JWK getJwk() {
        return this.jwk;
    }

    private static String sha256(byte[] data) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return new String(Hex.encode(md.digest(data)));
        } catch (NoSuchAlgorithmException e) {
            // should never happen
            throw new RuntimeException("Cannot obtain SHA-256 algorithm for message digest.", e);
        }
    }
}
