package run.halo.app.security.authentication;

import com.nimbusds.jose.jwk.JWK;
import reactor.core.publisher.Mono;

public interface CryptoService {

    /**
     * Decrypts message with Base64 format.
     *
     * @param encryptedMessage is a byte array containing encrypted message.
     * @return decrypted message.
     */
    Mono<byte[]> decrypt(byte[] encryptedMessage);

    /**
     * Reads public key.
     *
     * @return byte array of public key
     */
    Mono<byte[]> readPublicKey();

    /**
     * Gets key ID of private key.
     *
     * @return key ID of private key.
     */
    String getKeyId();

    /**
     * Gets JSON Web Keys.
     *
     * @return JSON Web Keys
     */
    JWK getJwk();

}
