package run.halo.app.security.authentication.impl;

import static com.nimbusds.jose.jwk.KeyOperation.SIGN;
import static com.nimbusds.jose.jwk.KeyOperation.VERIFY;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;
import reactor.core.Exceptions;
import reactor.test.StepVerifier;
import run.halo.app.security.authentication.login.InvalidEncryptedMessageException;

@ExtendWith(MockitoExtension.class)
class RsaKeyServiceTest {

    RsaKeyService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws JOSEException {
        service = new RsaKeyService(tempDir);
        service.afterPropertiesSet();
    }

    @Test
    void shouldGenerateKeyPair() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privKeyBytes = Files.readAllBytes(tempDir.resolve("pat_id_rsa"));
        byte[] pubKeyBytes = Files.readAllBytes(tempDir.resolve("pat_id_rsa.pub"));

        var pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
        var privKeySpec = new PKCS8EncodedKeySpec(privKeyBytes);
        var keyFactory = KeyFactory.getInstance(RsaKeyService.ALGORITHM);
        var privKey = (RSAPrivateCrtKey) keyFactory.generatePrivate(privKeySpec);
        var pubKey = (RSAPublicKey) keyFactory.generatePublic(pubKeySpec);
        assertEquals(privKey.getModulus(), pubKey.getModulus());
        assertEquals(privKey.getPublicExponent(), pubKey.getPublicExponent());
    }

    @Test
    void shouldReuseKeyPairOnRestart() throws JOSEException, IOException {
        var privateKeyBytes = Files.readAllBytes(tempDir.resolve("pat_id_rsa"));
        var publicKeyBytes = Files.readAllBytes(tempDir.resolve("pat_id_rsa.pub"));

        var restarted = new RsaKeyService(tempDir);
        restarted.afterPropertiesSet();

        assertEquals(service.getKeyId(), restarted.getKeyId());
        assertArrayEquals(privateKeyBytes, Files.readAllBytes(tempDir.resolve("pat_id_rsa")));
        assertArrayEquals(publicKeyBytes, Files.readAllBytes(tempDir.resolve("pat_id_rsa.pub")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "empty", "invalid", "mismatched"})
    void shouldRestorePublicKeyAndPreservePrivateKey(String state) throws Exception {
        var privateKeyBytes = Files.readAllBytes(tempDir.resolve("pat_id_rsa"));
        var publicKeyPath = tempDir.resolve("pat_id_rsa.pub");
        var publicKeyBytes = Files.readAllBytes(publicKeyPath);
        switch (state) {
            case "missing" -> Files.delete(publicKeyPath);
            case "empty" -> Files.write(publicKeyPath, new byte[0]);
            case "invalid" -> Files.write(publicKeyPath, new byte[] {1, 2, 3});
            case "mismatched" -> {
                var generator = KeyPairGenerator.getInstance(RsaKeyService.ALGORITHM);
                generator.initialize(2048);
                Files.write(
                        publicKeyPath, generator.generateKeyPair().getPublic().getEncoded());
            }
            default -> throw new AssertionError(state);
        }

        var restarted = new RsaKeyService(tempDir);
        restarted.afterPropertiesSet();

        assertEquals(service.getKeyId(), restarted.getKeyId());
        assertArrayEquals(privateKeyBytes, Files.readAllBytes(tempDir.resolve("pat_id_rsa")));
        assertArrayEquals(publicKeyBytes, Files.readAllBytes(publicKeyPath));
        service = restarted;
        shouldDecryptMessageCorrectly();
        shouldReuseKeyPairOnRestart();
    }

    @ParameterizedTest
    @CsvSource({"missing,true", "missing,false", "empty,true", "empty,false", "invalid,true", "invalid,false"})
    void shouldRegenerateKeyPairWhenPrivateKeyCannotBeRecovered(String state, boolean publicKeyExists)
            throws Exception {
        var privateKeyPath = tempDir.resolve("pat_id_rsa");
        switch (state) {
            case "missing" -> Files.delete(privateKeyPath);
            case "empty" -> Files.write(privateKeyPath, new byte[0]);
            case "invalid" -> Files.write(privateKeyPath, new byte[] {1, 2, 3});
            default -> throw new AssertionError(state);
        }
        if (!publicKeyExists) {
            Files.delete(tempDir.resolve("pat_id_rsa.pub"));
        }
        var previousKeyId = service.getKeyId();

        service = new RsaKeyService(tempDir);
        service.afterPropertiesSet();

        assertNotEquals(previousKeyId, service.getKeyId());
        shouldGenerateKeyPair();
        shouldDecryptMessageCorrectly();
        shouldReuseKeyPairOnRestart();
    }

    @Test
    void shouldPreserveMatchingNonCrtPrivateKey() throws Exception {
        var nonCrtKey = replacePrivateKeyWithNonCrtEncoding();
        var publicKeyBytes = Files.readAllBytes(tempDir.resolve("pat_id_rsa.pub"));

        service = new RsaKeyService(tempDir);
        service.afterPropertiesSet();

        assertArrayEquals(nonCrtKey.getEncoded(), Files.readAllBytes(tempDir.resolve("pat_id_rsa")));
        assertArrayEquals(publicKeyBytes, Files.readAllBytes(tempDir.resolve("pat_id_rsa.pub")));
        shouldDecryptMessageCorrectly();
        shouldReuseKeyPairOnRestart();
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "invalid", "mismatched"})
    void shouldRejectNonCrtPrivateKeyWithoutMatchingPublicKey(String state) throws Exception {
        var nonCrtKey = replacePrivateKeyWithNonCrtEncoding();
        var publicKeyPath = tempDir.resolve("pat_id_rsa.pub");
        var publicKeyBytes = Files.readAllBytes(publicKeyPath);
        switch (state) {
            case "missing" -> Files.delete(publicKeyPath);
            case "invalid" -> {
                publicKeyBytes = new byte[] {1, 2, 3};
                Files.write(publicKeyPath, publicKeyBytes);
            }
            case "mismatched" -> {
                var generator = KeyPairGenerator.getInstance(RsaKeyService.ALGORITHM);
                generator.initialize(2048);
                var mismatchedPublicKey =
                        generator.generateKeyPair().getPublic().getEncoded();
                Files.write(publicKeyPath, mismatchedPublicKey);
                publicKeyBytes = mismatchedPublicKey;
            }
            default -> throw new AssertionError(state);
        }
        var restarted = new RsaKeyService(tempDir);

        assertThrows(IllegalStateException.class, restarted::afterPropertiesSet);
        assertArrayEquals(nonCrtKey.getEncoded(), Files.readAllBytes(tempDir.resolve("pat_id_rsa")));
        if (Files.exists(publicKeyPath)) {
            assertArrayEquals(publicKeyBytes, Files.readAllBytes(publicKeyPath));
        }
    }

    private java.security.PrivateKey replacePrivateKeyWithNonCrtEncoding()
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        var privateKeyPath = tempDir.resolve("pat_id_rsa");
        var keyFactory = KeyFactory.getInstance(RsaKeyService.ALGORITHM);
        var crtKey = (RSAPrivateCrtKey)
                keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(privateKeyPath)));
        var nonCrtKey =
                keyFactory.generatePrivate(new RSAPrivateKeySpec(crtKey.getModulus(), crtKey.getPrivateExponent()));
        Files.write(privateKeyPath, nonCrtKey.getEncoded());
        return nonCrtKey;
    }

    @Test
    void shouldRecoverEmptyKeyFiles() throws Exception {
        Files.write(tempDir.resolve("pat_id_rsa"), new byte[0]);
        Files.write(tempDir.resolve("pat_id_rsa.pub"), new byte[0]);

        service = new RsaKeyService(tempDir);
        service.afterPropertiesSet();

        shouldGenerateKeyPair();
        shouldDecryptMessageCorrectly();
        shouldReuseKeyPairOnRestart();
    }

    @Test
    void shouldPropagateIoFailureWithoutReplacingPrivateKey() throws IOException {
        var privateKeyPath = tempDir.resolve("pat_id_rsa");
        var privateKeyBytes = Files.readAllBytes(privateKeyPath);
        var publicKeyPath = tempDir.resolve("pat_id_rsa.pub");
        Files.delete(publicKeyPath);
        Files.createDirectory(publicKeyPath);
        Files.createFile(publicKeyPath.resolve("entry"));
        var restarted = new RsaKeyService(tempDir);

        var error = assertThrows(RuntimeException.class, restarted::afterPropertiesSet);

        assertInstanceOf(IOException.class, error.getCause());
        assertArrayEquals(privateKeyBytes, Files.readAllBytes(privateKeyPath));
        assertTrue(Files.isDirectory(publicKeyPath));
        try (var files = Files.list(tempDir)) {
            assertEquals(
                    Set.of("pat_id_rsa", "pat_id_rsa.pub"),
                    files.map(path -> path.getFileName().toString()).collect(Collectors.toSet()));
        }
    }

    @Test
    void shouldRecoverAfterPublicKeyWriteFails() throws Exception {
        var privateKeyPath = tempDir.resolve("pat_id_rsa");
        var publicKeyPath = tempDir.resolve("pat_id_rsa.pub");
        Files.delete(privateKeyPath);
        Files.delete(publicKeyPath);
        var interrupted = new RsaKeyService(tempDir);
        var writeFailure = new IOException("Interrupted public key write");

        try (var files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            files.when(() -> Files.move(any(Path.class), eq(publicKeyPath), eq(ATOMIC_MOVE), eq(REPLACE_EXISTING)))
                    .thenThrow(writeFailure);

            var error = assertThrows(RuntimeException.class, interrupted::afterPropertiesSet);

            assertSame(writeFailure, error.getCause());
        }
        var privateKeyBytes = Files.readAllBytes(privateKeyPath);
        assertTrue(privateKeyBytes.length > 0);
        try (var files = Files.list(tempDir)) {
            assertEquals(
                    Set.of("pat_id_rsa"),
                    files.map(path -> path.getFileName().toString()).collect(Collectors.toSet()));
        }

        service = new RsaKeyService(tempDir);
        service.afterPropertiesSet();

        assertArrayEquals(privateKeyBytes, Files.readAllBytes(privateKeyPath));
        shouldGenerateKeyPair();
        shouldDecryptMessageCorrectly();
        shouldReuseKeyPairOnRestart();
    }

    @Test
    void shouldWriteKeyWhenAtomicMoveIsNotSupported() throws Exception {
        var publicKeyPath = tempDir.resolve("pat_id_rsa.pub");
        Files.delete(publicKeyPath);
        var restarted = new RsaKeyService(tempDir);

        try (var files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            files.when(() -> Files.move(any(Path.class), eq(publicKeyPath), eq(ATOMIC_MOVE), eq(REPLACE_EXISTING)))
                    .thenThrow(new AtomicMoveNotSupportedException("source", "target", "unsupported"));

            restarted.afterPropertiesSet();

            files.verify(() -> Files.move(any(Path.class), eq(publicKeyPath), eq(REPLACE_EXISTING)));
        }
        assertEquals(service.getKeyId(), restarted.getKeyId());
        service = restarted;
        shouldGenerateKeyPair();
        shouldDecryptMessageCorrectly();
    }

    @Test
    void shouldReadPublicKey() throws IOException {
        var realPubKeyBytes = Files.readAllBytes(tempDir.resolve("pat_id_rsa.pub"));

        StepVerifier.create(service.readPublicKey())
                .assertNext(bytes -> assertArrayEquals(realPubKeyBytes, bytes))
                .verifyComplete();
    }

    @Test
    void shouldDecryptMessageCorrectly() {
        final String message = "halo";

        var mono = service.readPublicKey()
                .map(pubKeyBytes -> {
                    var pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
                    try {
                        var keyFactory = KeyFactory.getInstance(RsaKeyService.ALGORITHM);
                        var pubKey = keyFactory.generatePublic(pubKeySpec);
                        var cipher = Cipher.getInstance(RsaKeyService.TRANSFORMATION);
                        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
                        return cipher.doFinal(message.getBytes());
                    } catch (NoSuchAlgorithmException
                            | InvalidKeySpecException
                            | NoSuchPaddingException
                            | InvalidKeyException
                            | IllegalBlockSizeException
                            | BadPaddingException e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .flatMap(service::decrypt)
                .map(String::new);

        StepVerifier.create(mono).expectNext(message).verifyComplete();
    }

    @Test
    void shouldFailToDecryptMessage() {
        StepVerifier.create(service.decrypt("invalid-bytes".getBytes()))
                .verifyError(InvalidEncryptedMessageException.class);
    }

    @Test
    void shouldGetKeyIdFromJwk() {
        assertTrue(StringUtils.hasText(service.getKeyId()));
    }

    @Test
    void shouldGetJwk() {
        var jwk = service.getJwk();
        assertEquals("RSA", jwk.getKeyType().getValue());
        assertEquals(JWSAlgorithm.RS256, jwk.getAlgorithm());
        assertEquals(KeyUse.SIGNATURE, jwk.getKeyUse());
        assertEquals(Set.of(SIGN, VERIFY), jwk.getKeyOperations());
    }
}
