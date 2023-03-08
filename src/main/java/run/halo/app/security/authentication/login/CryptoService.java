package run.halo.app.security.authentication.login;

import reactor.core.publisher.Mono;

public interface CryptoService {

    /**
     * Generates key pair.
     */
    Mono<Void> generateKeys();

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

}
