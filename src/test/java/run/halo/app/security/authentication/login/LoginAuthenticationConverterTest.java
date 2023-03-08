package run.halo.app.security.authentication.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LoginAuthenticationConverterTest {

    @Mock
    ServerWebExchange exchange;

    MultiValueMap<String, String> formData;

    @InjectMocks
    LoginAuthenticationConverter converter;

    @Mock
    CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        formData = new LinkedMultiValueMap<>();
        lenient().when(exchange.getFormData()).thenReturn(Mono.just(formData));
    }

    @Test
    void applyUsernameAndPasswordThenCreatesTokenSuccess() {
        var username = "username";
        var password = "password";
        var decryptedPassword = "decrypted password";

        formData.add("username", username);
        formData.add("password", Base64.getEncoder().encodeToString(password.getBytes()));

        when(cryptoService.decrypt(password.getBytes()))
            .thenReturn(Mono.just(decryptedPassword.getBytes()));
        StepVerifier.create(converter.convert(exchange))
            .assertNext(token -> {
                assertEquals(username, token.getPrincipal());
                assertEquals(decryptedPassword, token.getCredentials());
            })
            .verifyComplete();

        verify(cryptoService).decrypt(password.getBytes());
    }

    @Test
    void applyPasswordWithoutBase64FormatThenBadCredentialsException() {
        var username = "username";
        var password = "+invalid-base64-format-password";

        formData.add("username", username);
        formData.add("password", password);

        StepVerifier.create(converter.convert(exchange))
            .verifyError(BadCredentialsException.class);
    }

    @Test
    void applyUsernameAndInvalidPasswordThenBadCredentialsException() {
        var username = "username";
        var password = "password";

        formData.add("username", username);
        formData.add("password", Base64.getEncoder().encodeToString(password.getBytes()));

        when(cryptoService.decrypt(password.getBytes()))
            .thenReturn(Mono.error(() -> new InvalidEncryptedMessageException("invalid message")));
        StepVerifier.create(converter.convert(exchange))
                .verifyError(BadCredentialsException.class);
        verify(cryptoService).decrypt(password.getBytes());
    }

}