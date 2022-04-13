package run.halo.app.identity.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import lombok.Data;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * @author guqing
 * @date 2022-04-12
 */
@Data
public class AccessToken implements Serializable {
    private String tokenType;

    private Jwt accessToken;

    private Jwt refreshToken;
    private Map<String, Object> additionalInformation;

    @JsonIgnore
    private long expiration;

    public AccessToken(Jwt accessToken) {
        this.tokenType = "Bearer".toLowerCase();
        this.additionalInformation = Collections.emptyMap();
        this.accessToken = accessToken;
    }

    public long getExpiresIn() {
        return this.expiration;
    }
}
