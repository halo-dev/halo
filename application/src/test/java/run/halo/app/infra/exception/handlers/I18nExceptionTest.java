package run.halo.app.infra.exception.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@AutoConfigureWebTestClient
class I18nExceptionTest {

    @Autowired
    WebTestClient webClient;

    Locale currentLocale;

    @BeforeEach
    void setUp() {
        currentLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(currentLocale);
    }

    @Test
    void shouldBeOkForGreetingEndpoint() {
        webClient.get().uri("/response-entity/greet")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class).isEqualTo("Hello Halo");
    }

    @Test
    void shouldGetErrorIfErrorResponseThrow() {
        webClient.get().uri("/response-entity/error-response")
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(ProblemDetail.class)
            .value(problemDetail -> {
                assertEquals("Error Response", problemDetail.getTitle());
                assertEquals("Message argument is {0}.", problemDetail.getDetail());
            });
    }


    @Test
    void shouldGetErrorIfErrorResponseThrowWithMessageCode() {
        webClient.get().uri("/response-entity/error-response/with-message-code")
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(ProblemDetail.class)
            .value(problemDetail -> {
                assertEquals("Error Response", problemDetail.getTitle());
                assertEquals("Something went wrong, argument is fake-arg.",
                    problemDetail.getDetail());
            });
    }

    @Test
    void shouldGetErrorIfErrorResponseThrowWithMessageCodeAndLocaleIsChinese() {
        webClient.get().uri("/response-entity/error-response/with-message-code")
            .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh")
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(ProblemDetail.class)
            .value(problemDetail -> {
                assertEquals("发生错误", problemDetail.getTitle());
                assertEquals("发生了一些错误，参数：fake-arg。",
                    problemDetail.getDetail());
            });

    }

    @Test
    void shouldGetErrorIfThrowingResponseStatusException() {
        webClient.get().uri("/response-entity/with-response-status-error")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.GONE)
            .expectBody(ProblemDetail.class)
            .value(problemDetail -> {
                assertEquals("Gone", problemDetail.getTitle());
                assertEquals("Something went wrong",
                    problemDetail.getDetail());
            });
    }

    @Test
    void shouldGetErrorIfThrowingGeneralException() {
        // problem reason will be a fixed prompt when internal server error occurred.
        webClient.get().uri("/response-entity/general-error")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody(ProblemDetail.class)
            .value(problemDetail -> {
                assertEquals("Internal Server Error", problemDetail.getTitle());
                assertEquals("Something went wrong, please try again later.",
                    problemDetail.getDetail());
            });
    }

    @TestConfiguration
    static class TestConfig {

        @RestController
        @RequestMapping("/response-entity")
        static class ResponseEntityController {

            @GetMapping("/greet")
            ResponseEntity<String> greet() {
                return ResponseEntity.ok("Hello Halo");
            }

            @GetMapping("/error-response")
            ResponseEntity<String> throwErrorResponseException() {
                throw new ErrorResponseException();
            }

            @GetMapping("/error-response/with-message-args")
            ResponseEntity<String> throwErrorResponseExceptionWithMessageArgs() {
                throw new ErrorResponseException("Something went wrong.",
                    null, new Object[] {"fake-arg"});
            }

            @GetMapping("/error-response/with-message-code")
            ResponseEntity<String> throwErrorResponseExceptionWithMessageCode() {
                throw new ErrorResponseException("Something went wrong.",
                    "error.somethingWentWrong", new Object[] {"fake-arg"});
            }

            @GetMapping("/with-response-status-error")
            ResponseEntity<String> throwWithResponseStatusException() {
                throw new WithResponseStatusException();
            }

            @GetMapping("/general-error")
            ResponseEntity<String> throwGeneralException() {
                throw new GeneralException("Something went wrong");
            }

        }
    }

    static class ErrorResponseException extends ResponseStatusException {

        public ErrorResponseException() {
            this("Something went wrong.");
        }

        public ErrorResponseException(String reason) {
            this(reason, null, null);
        }

        public ErrorResponseException(String reason, String detailCode, Object[] detailArgs) {
            super(HttpStatus.BAD_REQUEST, reason, null, detailCode, detailArgs);
        }
    }

    @ResponseStatus(value = HttpStatus.GONE, reason = "Something went wrong")
    static class WithResponseStatusException extends RuntimeException {

    }

    static class GeneralException extends RuntimeException {

        public GeneralException(String message) {
            super(message);
        }
    }
}
