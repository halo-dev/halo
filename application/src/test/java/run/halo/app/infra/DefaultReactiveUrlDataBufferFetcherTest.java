package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.UnknownHostException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DefaultReactiveUrlDataBufferFetcherTest {

    @InjectMocks
    DefaultReactiveUrlDataBufferFetcher fetcher;

    @Test
    void fetchShouldReturnDataBuffers() {
        var response = ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body("hello world")
                .build();

        fetcher.setWebClient(WebClient.builder()
                .exchangeFunction(request -> Mono.just(response))
                .build());

        StepVerifier.create(fetcher.fetch(URI.create("http://example.com/file")))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void fetchShouldMapUnknownHostExceptionToServerWebInputException() {
        var cause = new UnknownHostException("nonexistent-host");
        var error = new WebClientRequestException(
                cause, HttpMethod.GET, URI.create("http://nonexistent-host/"), HttpHeaders.EMPTY);

        fetcher.setWebClient(WebClient.builder()
                .exchangeFunction(request -> Mono.error(error))
                .build());

        StepVerifier.create(fetcher.fetch(URI.create("http://nonexistent-host/")))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ServerWebInputException.class);
                    assertThat(e.getMessage()).contains("Unable to resolve host");
                    assertThat(e.getMessage()).contains("nonexistent-host");
                })
                .verify();
    }

    @Test
    void fetchShouldPassThroughOtherWebClientRequestExceptions() {
        var cause = new RuntimeException("connection refused");
        var error = new WebClientRequestException(
                cause, HttpMethod.GET, URI.create("http://example.com/"), HttpHeaders.EMPTY);

        fetcher.setWebClient(WebClient.builder()
                .exchangeFunction(request -> Mono.error(error))
                .build());

        StepVerifier.create(fetcher.fetch(URI.create("http://example.com/")))
                .expectError(WebClientRequestException.class)
                .verify();
    }

    @Test
    void headShouldReturnHeaders() {
        var response = ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header(HttpHeaders.CONTENT_LENGTH, "42")
                .body("")
                .build();

        fetcher.setWebClient(WebClient.builder()
                .exchangeFunction(request -> Mono.just(response))
                .build());

        StepVerifier.create(fetcher.head(URI.create("http://example.com/file")))
                .expectNextMatches(headers -> {
                    assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
                    assertThat(headers.getContentLength()).isEqualTo(42L);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void headShouldMapUnknownHostExceptionToServerWebInputException() {
        var cause = new UnknownHostException("nonexistent-host");
        var error = new WebClientRequestException(
                cause, HttpMethod.GET, URI.create("http://nonexistent-host/"), HttpHeaders.EMPTY);

        fetcher.setWebClient(WebClient.builder()
                .exchangeFunction(request -> Mono.error(error))
                .build());

        StepVerifier.create(fetcher.head(URI.create("http://nonexistent-host/")))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ServerWebInputException.class);
                    assertThat(e.getMessage()).contains("Unable to resolve host");
                })
                .verify();
    }

    @Test
    void headShouldPassThroughOtherWebClientRequestExceptions() {
        var cause = new RuntimeException("connection refused");
        var error = new WebClientRequestException(
                cause, HttpMethod.GET, URI.create("http://example.com/"), HttpHeaders.EMPTY);

        fetcher.setWebClient(WebClient.builder()
                .exchangeFunction(request -> Mono.error(error))
                .build());

        StepVerifier.create(fetcher.head(URI.create("http://example.com/")))
                .expectError(WebClientRequestException.class)
                .verify();
    }

    @Test
    void fetchResponseEntityShouldReturnEntityWithStatusAndHeaders() {
        var response = ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body("hello world")
                .build();

        fetcher.setWebClient(WebClient.builder()
                .exchangeFunction(request -> Mono.just(response))
                .build());

        StepVerifier.create(fetcher.fetchResponseEntity(URI.create("http://example.com/file")))
                .expectNextMatches(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(entity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void fetchResponseEntityShouldMapUnknownHostExceptionToServerWebInputException() {
        var cause = new UnknownHostException("nonexistent-host");
        var error = new WebClientRequestException(
                cause, HttpMethod.GET, URI.create("http://nonexistent-host/"), HttpHeaders.EMPTY);

        fetcher.setWebClient(WebClient.builder()
                .exchangeFunction(request -> Mono.error(error))
                .build());

        StepVerifier.create(fetcher.fetchResponseEntity(URI.create("http://nonexistent-host/")))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ServerWebInputException.class);
                    assertThat(e.getMessage()).contains("Unable to resolve host");
                })
                .verify();
    }

    @Test
    void fetchResponseEntityShouldPassThroughOtherWebClientRequestExceptions() {
        var cause = new RuntimeException("connection refused");
        var error = new WebClientRequestException(
                cause, HttpMethod.GET, URI.create("http://example.com/"), HttpHeaders.EMPTY);

        fetcher.setWebClient(WebClient.builder()
                .exchangeFunction(request -> Mono.error(error))
                .build());

        StepVerifier.create(fetcher.fetchResponseEntity(URI.create("http://example.com/")))
                .expectError(WebClientRequestException.class)
                .verify();
    }
}
