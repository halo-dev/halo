package run.halo.app.security.authentication.pat;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtPatDecoder implements PatDecoder {

    private final ReactiveJwtDecoder jwtDecoder;

    public JwtPatDecoder(ReactiveJwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Mono<Pat> decode(String token) {
        token = StringUtils.removeStart(token, "pat_");
        return jwtDecoder.decode(token)
            .map(JwtPat::new);
    }
}
