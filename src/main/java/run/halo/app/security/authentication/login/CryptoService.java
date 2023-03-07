package run.halo.app.security.authentication.login;

import reactor.core.publisher.Mono;

public interface CryptoService {

    /**
     * Re-generate key pair.
     */
    Mono<Void> regenerateKeys();

    /**
     * Decrypt message with Base64 format.
     *
     * @param encryptedMessage is a byte array containing encrypted message.
     * @return decrypted message.
     */
    Mono<byte[]> decrypt(byte[] encryptedMessage);

    /**
     * Read public key.
     *
     * @return byte array of public key
     */
    Mono<byte[]> readPublicKey();

}
