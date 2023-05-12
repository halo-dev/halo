package run.halo.app.core.extension.theme;

import java.io.InputStream;
import java.net.URI;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import run.halo.app.infra.utils.DataBufferUtils;

/**
 * Default zip stream fetcher.
 *
 * @author guqing
 * @since 2.6.0
 */
@Component
public class DefaultZipStreamFetcher implements ZipStreamFetcher {
    private final HttpClient httpClient = HttpClient.create()
        .followRedirect(true);
    private final WebClient webClient = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();

    @Override
    public Mono<InputStream> fetch(URI uri) {
        Flux<DataBuffer> dataBufferFlux = webClient.get()
            .uri(uri)
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .retrieve()
            .bodyToFlux(DataBuffer.class);
        return Mono.fromCallable(() -> DataBufferUtils.toInputStream(dataBufferFlux));
    }
}
