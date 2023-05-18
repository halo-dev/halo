package run.halo.app.infra;

import java.net.URI;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

/**
 * <p>A default implementation of {@link ReactiveUrlDataBufferFetcher}.</p>
 *
 * @author guqing
 * @since 2.6.0
 */
@Component
public class DefaultReactiveUrlDataBufferFetcher implements ReactiveUrlDataBufferFetcher {
    private final HttpClient httpClient = HttpClient.create()
        .followRedirect(true);
    private final WebClient webClient = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();

    @Override
    public Flux<DataBuffer> fetch(URI uri) {
        return webClient.get()
            .uri(uri)
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .retrieve()
            .bodyToFlux(DataBuffer.class);
    }
}
