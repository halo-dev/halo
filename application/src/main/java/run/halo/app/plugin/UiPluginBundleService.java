package run.halo.app.plugin;

import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Aggregates UI plugin bundles from started plugins and the activated theme. */
public interface UiPluginBundleService {

    Flux<DataBuffer> uglifyJsBundle();

    Flux<DataBuffer> uglifyCssBundle();

    Mono<String> generateBundleVersion();

    Mono<Resource> getJsBundle(String version);

    Mono<Resource> getCssBundle(String version);
}
