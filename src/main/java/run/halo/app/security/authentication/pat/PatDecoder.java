package run.halo.app.security.authentication.pat;

import reactor.core.publisher.Mono;

public interface PatDecoder {

    Mono<Pat> decode(String token);

}
