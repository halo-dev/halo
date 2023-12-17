package run.halo.app.security.authentication.login.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.Exceptions;
import reactor.test.StepVerifier;
import run.halo.app.security.authentication.login.InvalidEncryptedMessageException;

@ExtendWith(MockitoExtension.class)
class RsaKeyServiceTest {

    RsaKeyService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new RsaKeyService(tempDir);
    }

    @Test
    void shouldGenerateKeyPair()
        throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        StepVerifier.create(service.generateKeys())
            .verifyComplete();
        // check the file
        byte[] privKeyBytes = Files.readAllBytes(tempDir.resolve("id_rsa"));
        byte[] pubKeyBytes = Files.readAllBytes(tempDir.resolve("id_rsa.pub"));

        var pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
        var privKeySpec = new PKCS8EncodedKeySpec(privKeyBytes);
        var keyFactory = KeyFactory.getInstance(RsaKeyService.ALGORITHM);
        var privKey = (RSAPrivateCrtKey) keyFactory.generatePrivate(privKeySpec);
        var pubKey = (RSAPublicKey) keyFactory.generatePublic(pubKeySpec);
        assertEquals(privKey.getModulus(), pubKey.getModulus());
        assertEquals(privKey.getPublicExponent(), pubKey.getPublicExponent());
    }

    @Test
    void shouldReadPublicKey() throws IOException {
        StepVerifier.create(service.generateKeys())
            .verifyComplete();

        var realPubKeyBytes = Files.readAllBytes(tempDir.resolve("id_rsa.pub"));

        StepVerifier.create(service.readPublicKey())
            .assertNext(bytes -> assertArrayEquals(realPubKeyBytes, bytes))
            .verifyComplete();
    }

    @Test
    void shouldDecryptMessageCorrectly() {
        StepVerifier.create(service.generateKeys())
            .verifyComplete();

        final String message = "halo";

        var mono = service.readPublicKey()
            .map(pubKeyBytes -> {
                var pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
                try {
                    var keyFactory = KeyFactory.getInstance(RsaKeyService.ALGORITHM);
                    var pubKey = keyFactory.generatePublic(pubKeySpec);
                    var cipher = Cipher.getInstance(RsaKeyService.ALGORITHM);
                    cipher.init(Cipher.ENCRYPT_MODE, pubKey);
                    return cipher.doFinal(message.getBytes());
                } catch (NoSuchAlgorithmException | InvalidKeySpecException
                         | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                         | BadPaddingException e) {
                    throw Exceptions.propagate(e);
                }
            })
            .flatMap(service::decrypt)
            .map(String::new);

        StepVerifier.create(mono)
            .expectNext(message)
            .verifyComplete();
    }

    @Test
    void shouldFailToDecryptMessage() {
        StepVerifier.create(service.generateKeys())
            .verifyComplete();

        StepVerifier.create(service.decrypt("invalid-bytes".getBytes()))
            .verifyError(InvalidEncryptedMessageException.class);
    }
}