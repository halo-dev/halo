package run.halo.app.theme.engine;

import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.Charset;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.thymeleaf.context.IContext;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Default template engine implementation to be used in Halo.
 *
 * @author johnniang
 */
@Slf4j
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
            return mono.subscribeOn(Schedulers.boundedElastic())
                .doOnError(Exception.class, e -> this.logTemplateError(e, template));
        }
        if (publisher instanceof Flux<DataBuffer> flux) {
            return flux.subscribeOn(Schedulers.boundedElastic())
                .doOnError(Exception.class, e -> this.logTemplateError(e, template));
        }
        return publisher;
    }

    private void logTemplateError(Exception e, String template) {
        if (Exceptions.unwrap(e.getCause()) instanceof InterruptedException) {
            log.warn("Interrupted while processing template: {}", template);
        }
        if (e.getCause() instanceof ClosedByInterruptException) {
            log.warn("Interrupted while outputting template: {}", template);
        }
        // other exceptions will be caught by error handler
    }
}
