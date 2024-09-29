package run.halo.app.security.jackson2;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import run.halo.app.security.authentication.oauth2.HaloOAuth2AuthenticationToken;

/**
 * Mixin for {@link HaloOAuth2AuthenticationToken}.
 *
 * @author johnniang
 * @since 2.20.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class HaloOAuth2AuthenticationTokenMixin {

    @JsonCreator
    HaloOAuth2AuthenticationTokenMixin(
        @JsonProperty("userDetails") UserDetails userDetails,
        @JsonProperty("original") OAuth2AuthenticationToken original
    ) {
    }
}
