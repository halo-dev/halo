package run.halo.app.security.authentication.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PublicKeyRouteBuilderTest {

    WebTestClient webClient;

    @Mock
    CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(
            new PublicKeyRouteBuilder(cryptoService).build()
        ).build();
    }

    @Test
    void shouldReadPublicKey() {
        var publicKeyStr = "public-key";
        var encoder = Base64.getEncoder();
        when(cryptoService.readPublicKey()).thenReturn(Mono.just(publicKeyStr.getBytes()));
        webClient.get().uri("/login/public-key")
            .exchange()
            .expectStatus().isOk()
            .expectBody(PublicKeyRouteBuilder.PublicKeyResponse.class)
            .consumeWith(result -> {
                var response = result.getResponseBody();
                assertNotNull(response);
                assertEquals(encoder.encodeToString(publicKeyStr.getBytes()),
                    response.getBase64Format());
            });

        verify(cryptoService).readPublicKey();
    }

}
