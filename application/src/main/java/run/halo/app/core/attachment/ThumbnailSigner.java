package run.halo.app.core.attachment;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ThumbnailSigner {
    private static final String ALGORITHM = "SHA-256";

    /**
     * Generate signature for the given input.
     *
     * @param input generally the uri of the thumbnail
     */
    public static String generateSignature(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);

            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            BigInteger number = new BigInteger(1, hashBytes);
            StringBuilder hexString = new StringBuilder(number.toString(16));

            // Complete the string to ensure a length of 64 characters
            while (hexString.length() < 64) {
                hexString.insert(0, '0');
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(ALGORITHM + " algorithm not found", e);
        }
    }

    public static String generateSignature(URI uri) {
        return generateSignature(uri.toASCIIString());
    }
}
