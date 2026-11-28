package run.halo.app.migration.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-256-GCM encryption for backup packages.
 *
 * <p>The encrypted stream starts with a fixed header: {@code magic(8) | iterations(4) | salt(16) | iv(12)} followed by
 * the encrypted payload and the authentication tag.
 */
final class BackupEncryption {

    private static final byte[] MAGIC = "HALOENC1".getBytes(StandardCharsets.US_ASCII);
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 210_000;
    private static final int MIN_ITERATIONS = 10_000;
    private static final int MAX_ITERATIONS = 1_000_000;
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 256;
    private static final int TAG_LENGTH_BITS = 128;
    static final int HEADER_LENGTH = MAGIC.length + Integer.BYTES + SALT_LENGTH + IV_LENGTH;

    private static final SecureRandom RANDOM = new SecureRandom();

    private BackupEncryption() {}

    /**
     * Checks whether the given prefix starts with the encrypted backup magic bytes.
     *
     * @param prefix the first bytes of a backup file
     * @return true if the content is encrypted by {@link #encrypt}
     */
    static boolean isEncrypted(byte[] prefix) {
        if (prefix.length < MAGIC.length) {
            return false;
        }
        return Arrays.equals(prefix, 0, MAGIC.length, MAGIC, 0, MAGIC.length);
    }

    /**
     * Writes the encryption header to the given output and returns a stream for encrypted content.
     *
     * @param output the target output stream
     * @param password the encryption password
     * @return a stream that encrypts written bytes
     * @throws IOException if the header cannot be written or the cipher cannot be initialized
     */
    static OutputStream encrypt(OutputStream output, String password) throws IOException {
        var salt = randomBytes(SALT_LENGTH);
        var iv = randomBytes(IV_LENGTH);
        var header = header(ITERATIONS, salt, iv);
        output.write(header);
        var cipher = initCipher(Cipher.ENCRYPT_MODE, password, header, salt, iv, ITERATIONS);
        return new CipherOutputStream(output, cipher);
    }

    /**
     * Decrypts the remaining content after the header has been read and verified.
     *
     * @param input the stream positioned right after the header
     * @param header the header read from the stream
     * @param password the decryption password
     * @return a stream that decrypts read bytes
     * @throws IOException if the header is invalid or the cipher cannot be initialized
     */
    static InputStream decrypt(InputStream input, byte[] header, String password) throws IOException {
        var buffer = ByteBuffer.wrap(header, MAGIC.length, HEADER_LENGTH - MAGIC.length);
        var iterations = buffer.getInt();
        if (iterations < MIN_ITERATIONS || iterations > MAX_ITERATIONS) {
            throw new IOException("Unsupported backup encryption parameters.");
        }
        var salt = new byte[SALT_LENGTH];
        buffer.get(salt);
        var iv = new byte[IV_LENGTH];
        buffer.get(iv);
        var cipher = initCipher(Cipher.DECRYPT_MODE, password, header, salt, iv, iterations);
        return new CipherInputStream(input, cipher);
    }

    private static byte[] header(int iterations, byte[] salt, byte[] iv) {
        return ByteBuffer.allocate(HEADER_LENGTH)
                .put(MAGIC)
                .putInt(iterations)
                .put(salt)
                .put(iv)
                .array();
    }

    private static Cipher initCipher(int mode, String password, byte[] header, byte[] salt, byte[] iv, int iterations)
            throws IOException {
        try {
            var factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            var keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);
            byte[] keyBytes;
            try {
                keyBytes = factory.generateSecret(keySpec).getEncoded();
            } finally {
                keySpec.clearPassword();
            }
            try {
                var cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(mode, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
                cipher.updateAAD(header);
                return cipher;
            } finally {
                Arrays.fill(keyBytes, (byte) 0);
            }
        } catch (GeneralSecurityException e) {
            throw new IOException("Failed to initialize backup encryption.", e);
        }
    }

    private static byte[] randomBytes(int length) {
        var bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
