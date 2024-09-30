package run.halo.app.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DefaultServerAuthenticationEntryPointTest {

    @Mock
    ServerRequestCache requestCache;

    @InjectMocks
    DefaultServerAuthenticationEntryPoint entryPoint;

    @Test
    void commenceForXhrRequest() {
        var mockReq = MockServerHttpRequest.get("/protected")
            .header("X-Requested-With", "XMLHttpRequest")
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

    @Test
    void commenceForNormalRequest() {
        var mockReq = MockServerHttpRequest.get("/protected")
            .build();
        var mockExchange = MockServerWebExchange.builder(mockReq)
            .build();
        Mockito.when(requestCache.saveRequest(mockExchange)).thenReturn(Mono.empty());
        var commenceMono = entryPoint.commence(mockExchange,
            new AuthenticationCredentialsNotFoundException("Not Found"));
        StepVerifier.create(commenceMono)
            .verifyComplete();
        assertEquals(URI.create("/login?authentication_required"),
            mockExchange.getResponse().getHeaders().getLocation());
    }
}