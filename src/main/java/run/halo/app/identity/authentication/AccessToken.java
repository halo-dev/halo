package run.halo.app.identity.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import lombok.Data;

/**
 * @author guqing
 * @date 2022-04-12
 */
@Data
public class AccessToken implements Serializable {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Map<String, Object> additionalInformation;

    @JsonIgnore
    private long expiration;

    public AccessToken(String accessToken) {
        this.tokenType = "Bearer".toLowerCase();
        this.additionalInformation = Collections.emptyMap();
        this.accessToken = accessToken;
    }

    public long getExpiresIn() {
        return this.expiration;
    }
}