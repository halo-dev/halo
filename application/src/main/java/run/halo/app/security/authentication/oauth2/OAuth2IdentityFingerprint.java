package run.halo.app.security.authentication.oauth2;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public final class OAuth2IdentityFingerprint {

    public static final String PARAMETER_NAME = "oauth2Identity";

    private OAuth2IdentityFingerprint() {}

    public static String from(OAuth2AuthenticationToken token) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            digest.update(token.getAuthorizedClientRegistrationId().getBytes(UTF_8));
            digest.update((byte) 0);
            digest.update(token.getPrincipal().getName().getBytes(UTF_8));
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
