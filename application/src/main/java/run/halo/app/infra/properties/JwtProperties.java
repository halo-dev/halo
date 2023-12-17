package run.halo.app.infra.properties;

import jakarta.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.validation.annotation.Validated;

/**
 * @author guqing
 * @author johnniang
 * @date 2022-04-12
 */
@Validated
public class JwtProperties {

    /**
     * URI that can either be an OpenID Connect discovery endpoint or an OAuth 2.0
     * Authorization Server Metadata endpoint defined by RFC 8414.
     */
    private String issuerUri;

    /**
     * JSON Web Algorithm used for verifying the digital signatures.
     */
    private SignatureAlgorithm jwsAlgorithm;

    /**
     * Location of the file containing the public key used to verify a JWT.
     */
    @NotNull
    private Resource publicKeyLocation;

    @NotNull
    private Resource privateKeyLocation;

    private final RSAPrivateKey privateKey;

    private final RSAPublicKey publicKey;

    public JwtProperties(String issuerUri, SignatureAlgorithm jwsAlgorithm,
        Resource publicKeyLocation,
        Resource privateKeyLocation) throws IOException {
        this.issuerUri = issuerUri;
        this.jwsAlgorithm = jwsAlgorithm;
        if (jwsAlgorithm == null) {
            this.jwsAlgorithm = SignatureAlgorithm.RS256;
        }
        this.publicKeyLocation = publicKeyLocation;
        this.privateKeyLocation = privateKeyLocation;

        //TODO initialize private and public keys at first startup.
        this.privateKey = this.readPrivateKey();
        this.publicKey = this.readPublicKey();
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public SignatureAlgorithm getJwsAlgorithm() {
        return this.jwsAlgorithm;
    }

    public void setJwsAlgorithm(SignatureAlgorithm jwsAlgorithm) {
        this.jwsAlgorithm = jwsAlgorithm;
    }

    public Resource getPublicKeyLocation() {
        return this.publicKeyLocation;
    }

    public void setPublicKeyLocation(Resource publicKeyLocation) {
        this.publicKeyLocation = publicKeyLocation;
    }

    public Resource getPrivateKeyLocation() {
        return privateKeyLocation;
    }

    public void setPrivateKeyLocation(Resource privateKeyLocation) {
        this.privateKeyLocation = privateKeyLocation;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    private RSAPublicKey readPublicKey() throws IOException {
        String key = "halo.security.oauth2.jwt.public-key-location";
        Assert.notNull(this.publicKeyLocation, "PublicKeyLocation must not be null");
        if (!this.publicKeyLocation.exists()) {
            throw new InvalidConfigurationPropertyValueException(key, this.publicKeyLocation,
                "Public key location does not exist");
        }
        try (InputStream inputStream = this.publicKeyLocation.getInputStream()) {
            String source = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            return RsaKeyConverters.x509()
                .convert(new ByteArrayInputStream(source.getBytes()));
        }
    }

    private RSAPrivateKey readPrivateKey() throws IOException {
        String key = "halo.security.oauth2.jwt.private-key-location";
        Assert.notNull(this.privateKeyLocation, "PrivateKeyLocation must not be null");
        if (!this.privateKeyLocation.exists()) {
            throw new InvalidConfigurationPropertyValueException(key, this.privateKeyLocation,
                "Private key location does not exist");
        }
        try (InputStream inputStream = this.privateKeyLocation.getInputStream()) {
            String source = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            return RsaKeyConverters.pkcs8()
                .convert(new ByteArrayInputStream(source.getBytes()));
        }
    }
}
