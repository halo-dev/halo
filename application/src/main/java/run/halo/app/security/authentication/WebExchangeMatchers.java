package run.halo.app.security.authentication;

import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

public enum WebExchangeMatchers {
    ;

    public static ServerWebExchangeMatcher ignoringMediaTypeAll(MediaType... matchingMediaTypes) {
        var matcher = new MediaTypeServerWebExchangeMatcher(matchingMediaTypes);
        matcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));
        return matcher;
    }
}
