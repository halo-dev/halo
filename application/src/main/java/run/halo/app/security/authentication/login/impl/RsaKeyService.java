package run.halo.app.security.authentication.login.impl;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Set;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.util.StopWatch;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.security.authentication.login.CryptoService;
import run.halo.app.security.authentication.login.InvalidEncryptedMessageException;

@Slf4j
public class RsaKeyService implements CryptoService {

    public static final String ALGORITHM = "RSA";

    private final Path privateKeyPath;

    private final Path publicKeyPath;

    public RsaKeyService(Path dir) {
        privateKeyPath = dir.resolve("id_rsa");
        publicKeyPath = dir.resolve("id_rsa.pub");
    }

    @Override
    public Mono<Void> generateKeys() {
        try {
            log.info("Generating RSA keys...");
            var stopWatch = new StopWatch("GenerateRSAKeys");
            stopWatch.start();
            var generator = KeyPairGenerator.getInstance(ALGORITHM);
            generator.initialize(2048);
            var keyPair = generator.generateKeyPair();
            stopWatch.stop();
            log.info("Generated RSA keys. Usage: {} ms.", stopWatch.getTotalTimeMillis());

            var dataBufferFactory = DefaultDataBufferFactory.sharedInstance;
            var privateKeyDataBuffer = Mono.<DataBuffer>fromSupplier(() ->
                dataBufferFactory.wrap(keyPair.getPrivate().getEncoded()));
            var publicKeyDataBuffer = Mono.<DataBuffer>fromSupplier(() ->
                dataBufferFactory.wrap(keyPair.getPublic().getEncoded()));

            var writePrivateKey =
                DataBufferUtils.write(privateKeyDataBuffer, privateKeyPath, TRUNCATE_EXISTING);
            var writePublicKey =
                DataBufferUtils.write(publicKeyDataBuffer, publicKeyPath, TRUNCATE_EXISTING);

            return Mono.when(
                    createFileIfNotExist(privateKeyPath),
                    createFileIfNotExist(publicKeyPath))
                .then(Mono.when(
                    writePrivateKey,
                    writePublicKey));
        } catch (NoSuchAlgorithmException e) {
            return Mono.error(e);
        }
    }

    @Override
    public Mono<byte[]> decrypt(byte[] encryptedMessage) {
        return readKey(privateKeyPath)
            .map(privateKeyBytes -> {
                var keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                try {
                    var keyFactory = KeyFactory.getInstance(ALGORITHM);
                    var privateKey = keyFactory.generatePrivate(keySpec);
                    var cipher = Cipher.getInstance(ALGORITHM);
                    cipher.init(Cipher.DECRYPT_MODE, privateKey);
                    return cipher.doFinal(encryptedMessage);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException
                         | NoSuchPaddingException | InvalidKeyException e) {
                    throw new RuntimeException("Failed to read private key or the key was invalid.",
                        e);
                } catch (IllegalBlockSizeException | BadPaddingException e) {
                    // invalid encrypted message
                    throw new InvalidEncryptedMessageException("Invalid encrypted message.", e);
                }
            });
    }

    @Override
    public Mono<byte[]> readPublicKey() {
        return readKey(publicKeyPath);
    }

    private Mono<byte[]> readKey(Path keyPath) {
        var content =
            DataBufferUtils.read(keyPath, DefaultDataBufferFactory.sharedInstance, 4096);

        return DataBufferUtils.join(content)
            .map(dataBuffer -> {
                // the byte count won't be too large
                var byteBuffer = ByteBuffer.allocate(dataBuffer.readableByteCount());
                dataBuffer.toByteBuffer(byteBuffer);
                return byteBuffer.array();
            });
    }

    Mono<Void> createFileIfNotExist(Path path) {
        if (Files.notExists(path)) {
            return Mono.fromRunnable(() -> {
                try {
                    Files.createDirectories(path.getParent());
                    if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
                        Files.createFile(path,
                            PosixFilePermissions.asFileAttribute(Set.of(OWNER_READ, OWNER_WRITE)));
                    } else {
                        Files.createFile(path);
                    }
                } catch (IOException e) {
                    // ignore the error
                    log.warn("Failed to create file for {}", path, e);
                }
            }).subscribeOn(Schedulers.boundedElastic()).then();
        }
        return Mono.empty();
    }
}
