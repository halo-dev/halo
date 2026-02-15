package run.halo.app.infra;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Set;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * <p>A default implementation of {@link ReactiveUrlDataBufferFetcher}.</p>
 *
 * @author guqing
 * @since 2.6.0
 */
@Component
public class DefaultReactiveUrlDataBufferFetcher implements ReactiveUrlDataBufferFetcher {
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    private final HttpClient httpClient = HttpClient.create()
        .followRedirect(true);
    private final ContentLengthFetcher contentLengthFetcher = new ContentLengthFetcher();

    private final WebClient webClient = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();

    @Override
    public Flux<DataBuffer> fetch(URI uri) {
        return Mono.fromCallable(() -> {
                validateUri(uri);
                return uri;
            })
            .flatMapMany(validatedUri -> webClient.get()
                .uri(validatedUri)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToFlux(DataBuffer.class));
    }

    @Override
    public Mono<HttpHeaders> head(URI uri) {
        return Mono.fromCallable(() -> {
                validateUri(uri);
                return uri;
            })
            .flatMap(contentLengthFetcher::fetchContentLength);
    }

    static void validateUri(URI uri) {
        if (uri == null) {
            throw new ServerWebInputException("URI must not be null.");
        }
        var scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new ServerWebInputException(
                "Only http and https schemes are allowed, got: " + scheme);
        }
        var host = uri.getHost();
        if (host == null) {
            throw new ServerWebInputException("URI must have a valid host.");
        }
        InetAddress address;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new ServerWebInputException("Unable to resolve host: " + host);
        }
        if (address.isLoopbackAddress()
            || address.isSiteLocalAddress()
            || address.isLinkLocalAddress()
            || address.isAnyLocalAddress()) {
            throw new ServerWebInputException(
                "Requests to private or loopback addresses are not allowed.");
        }
    }

    static class ContentLengthFetcher {

        private final WebClient webClient;

        ContentLengthFetcher() {
            this.webClient = WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(config -> config.defaultCodecs().maxInMemorySize(1))
                    .build())
                .build();
        }

        Mono<HttpHeaders> fetchContentLength(URI url) {
            return webClient.get()
                .uri(url)
                .exchangeToMono(response -> {
                    HttpHeaders headers = response.headers().asHttpHeaders();

                    return response.bodyToMono(byte[].class)
                        .onErrorResume(ex -> Mono.empty())
                        .thenReturn(headers);
                });
        }
    }
}
