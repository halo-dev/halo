package run.halo.app.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DefaultServerAuthenticationEntryPointTest {

    @InjectMocks
    DefaultServerAuthenticationEntryPoint entryPoint;

    @Test
    void commence() {
        var mockReq = MockServerHttpRequest.get("/protected")
            .build();
        var mockExchange = MockServerWebExchange.builder(mockReq)
            .build();
        var commenceMono = entryPoint.commence(mockExchange,
            new AuthenticationCredentialsNotFoundException("Not Found"));
        StepVerifier.create(commenceMono)
            .verifyComplete();
        var headers = mockExchange.getResponse().getHeaders();
        assertEquals("FormLogin realm=\"console\"", headers.getFirst(WWW_AUTHENTICATE));
    }

}