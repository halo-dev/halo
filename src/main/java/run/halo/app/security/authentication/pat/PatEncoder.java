package run.halo.app.security.authentication.pat;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.pat.PersonalAccessToken;

public interface PatEncoder {

    Mono<String> buildToken(PersonalAccessToken pat);

    Mono<String> encode(String rawToken);

    Mono<Boolean> matches(String rawToken, String encodedToken);

}
