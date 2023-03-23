package run.halo.app.theme.engine;

import java.nio.charset.Charset;
import java.util.Set;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.thymeleaf.context.IContext;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Default template engine implementation to be used in Halo.
 *
 * @author johnniang
 */
public class HaloTemplateEngine extends SpringWebFluxTemplateEngine {

    private final IMessageResolver messageResolver;

    public HaloTemplateEngine(IMessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    @Override
    protected void initializeSpringSpecific() {
        // Before initialization, thymeleaf will overwrite message resolvers.
        // So we need to add own message resolver at here.
        addMessageResolver(messageResolver);
    }

    @Override
    public Publisher<DataBuffer> processStream(String template, Set<String> markupSelectors,
        IContext context, DataBufferFactory bufferFactory,
        MediaType mediaType, Charset charset, int responseMaxChunkSizeBytes) {
        var publisher = super.processStream(template, markupSelectors, context, bufferFactory,
            mediaType, charset, responseMaxChunkSizeBytes);
        // We have to subscribe on blocking thread, because some blocking operations will be present
        // while processing.
        if (publisher instanceof Mono<DataBuffer> mono) {
            return mono.subscribeOn(Schedulers.boundedElastic());
        }
        if (publisher instanceof Flux<DataBuffer> flux) {
            return flux.subscribeOn(Schedulers.boundedElastic());
        }
        return publisher;
    }

}
