package run.halo.app.security.authentication.twofactor.totp;

public interface TotpAuthService {

    boolean validateTotp(String rawSecret, int code);

    String generateTotpSecret();

    String encryptSecret(String rawSecret);

    String decryptSecret(String encryptedSecret);

}
