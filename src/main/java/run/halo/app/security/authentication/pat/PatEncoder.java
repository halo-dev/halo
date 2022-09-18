package run.halo.app.security.authentication.pat;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.pat.PersonalAccessToken;

public interface PatEncoder {

    Mono<String> encode(PersonalAccessToken pat);

    boolean matches(String rawToken, String encodedToken);

}
