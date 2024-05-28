package run.halo.app.security.jackson2;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.core.userdetails.UserDetails;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility =
    JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class HaloUserMixin {

    HaloUserMixin(@JsonProperty("delegate") UserDetails delegate,
        @JsonProperty("twoFactorAuthEnabled") boolean twoFactorAuthEnabled,
        @JsonProperty("totpEncryptedSecret") String totpEncryptedSecret) {
    }

}
