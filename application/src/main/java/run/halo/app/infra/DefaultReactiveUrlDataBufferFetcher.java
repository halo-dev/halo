package run.halo.app.infra;

import java.net.URI;
import java.net.UnknownHostException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.utils.HttpSecurityUtils;

/**
 * <p>A default implementation of {@link ReactiveUrlDataBufferFetcher}.</p>
 *
 * @author guqing
 * @since 2.6.0
 */
@Component
class DefaultReactiveUrlDataBufferFetcher implements ReactiveUrlDataBufferFetcher {

    private final WebClient webClient = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(HttpSecurityUtils.secureHttpClient()))
        .build();

    @Override
    public Flux<DataBuffer> fetch(URI uri) {
        return webClient.get()
            .uri(uri)
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .retrieve()
            .bodyToFlux(DataBuffer.class)
            .onErrorMap(
                WebClientRequestException.class,
                DefaultReactiveUrlDataBufferFetcher::mapToErrorResponse
            );
    }

    @Override
    public Mono<HttpHeaders> head(URI uri) {
        return webClient.get().uri(uri)
            .retrieve()
            .toBodilessEntity()
            .map(HttpEntity::getHeaders)
            .onErrorMap(
                WebClientRequestException.class,
                DefaultReactiveUrlDataBufferFetcher::mapToErrorResponse
            );
    }

    @Override
    public Mono<ResponseEntity<Flux<DataBuffer>>> fetchResponseEntity(URI uri) {
        return webClient.get().uri(uri)
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .retrieve()
            .toEntityFlux(DataBuffer.class)
            .onErrorMap(
                WebClientRequestException.class,
                DefaultReactiveUrlDataBufferFetcher::mapToErrorResponse
            );
    }

    private static Throwable mapToErrorResponse(WebClientRequestException ex) {
        if (ex.getCause() instanceof UnknownHostException uhe) {
            return new ServerWebInputException(
                "Unable to resolve host or private IP resolved: " + uhe.getMessage()
            );
        }
        return ex;
    }

}
