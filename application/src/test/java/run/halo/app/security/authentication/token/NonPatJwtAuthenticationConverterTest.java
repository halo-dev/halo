package run.halo.app.security.authentication.token;

import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class NonPatJwtAuthenticationConverterTest {

    @InjectMocks NonPatJwtAuthenticationConverter converter;

    @Mock Converter<Jwt, Mono<AbstractAuthenticationToken>> delegate;

    @Test
    void shouldNotConvertIfJwtIsPat() {
        var jwt =
                Jwt.withTokenValue("token")
                        .claim("pat_name", "test-pat")
                        .header("alg", "none")
                        .build();
        requireNonNull(converter.convert(jwt))
                .as(StepVerifier::create)
                .expectError(InvalidBearerTokenException.class)
                .verify();
        verify(delegate, never()).convert(jwt);
    }

    @Test
    void shouldConvertJwtCorrectly() {
        var jwt = Jwt.withTokenValue("token").claim("a", "b").header("alg", "none").build();

        var expectToken = mock(AbstractAuthenticationToken.class);
        when(delegate.convert(jwt)).thenReturn(Mono.just(expectToken));

        requireNonNull(converter.convert(jwt))
                .as(StepVerifier::create)
                .expectNext(expectToken)
                .verifyComplete();
    }
}
