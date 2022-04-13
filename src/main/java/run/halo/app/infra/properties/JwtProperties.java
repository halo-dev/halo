package run.halo.app.infra.properties;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

/**
 * @author guqing
 * @date 2022-04-12
 */
@ConfigurationProperties(prefix = "halo.security.oauth2.jwt")
public class JwtProperties {

    /**
     * URI that can either be an OpenID Connect discovery endpoint or an OAuth 2.0
     * Authorization Server Metadata endpoint defined by RFC 8414.
     */
    private String issuerUri;

    /**
     * JSON Web Algorithm used for verifying the digital signatures.
     */
    private String jwsAlgorithm = "RS256";

    /**
     * Location of the file containing the public key used to verify a JWT.
     */
    private Resource publicKeyLocation;

    private Resource privateKeyLocation;

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getJwsAlgorithm() {
        return this.jwsAlgorithm;
    }

    public void setJwsAlgorithm(String jwsAlgorithm) {
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

    public RSAPublicKey readPublicKey() throws IOException {
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

    public RSAPrivateKey readPrivateKey() throws IOException {
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
