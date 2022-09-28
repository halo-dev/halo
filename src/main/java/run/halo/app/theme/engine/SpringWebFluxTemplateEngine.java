package run.halo.app.theme.engine;

import static run.halo.app.theme.engine.SpringWebFluxTemplateEngine.DataDrivenFluxStep.FluxStepPhase.DATA_DRIVEN_PHASE_BUFFER;
import static run.halo.app.theme.engine.SpringWebFluxTemplateEngine.DataDrivenFluxStep.FluxStepPhase.DATA_DRIVEN_PHASE_HEAD;
import static run.halo.app.theme.engine.SpringWebFluxTemplateEngine.DataDrivenFluxStep.FluxStepPhase.DATA_DRIVEN_PHASE_TAIL;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.thymeleaf.IThrottledTemplateProcessor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.engine.DataDrivenTemplateIterator;
import org.thymeleaf.engine.ISSEThrottledTemplateWriterControl;
import org.thymeleaf.engine.IThrottledTemplateWriterControl;
import org.thymeleaf.engine.ThrottledTemplateProcessor;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring6.ISpringWebFluxTemplateEngine;
import org.thymeleaf.spring6.context.Contexts;
import org.thymeleaf.spring6.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring6.context.webflux.IReactiveSSEDataDriverContextVariable;
import org.thymeleaf.util.LoggingUtils;
import org.thymeleaf.web.IWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


/**
 * <p>
 * Standard implementation of {@link ISpringWebFluxTemplateEngine}, and default
 * template engine implementation to be used in Spring WebFlux environments.
 * </p>
 * Code from
 * <a href="https://github.com/thymeleaf/thymeleaf/blob/3.1-master/lib/thymeleaf-spring6/src/main/java/org/thymeleaf/spring6/SpringWebFluxTemplateEngine.java">thymeleaf SpringWebFluxTemplateEngine</a>
 *
 * <p>Note that: We need to subscribe on a new thread to support blocking operations
 * for theme finders in {@link #createDataDrivenStream} and {@link #createFullStream} and
 * {@link #createChunkedStream}.</p>
 *
 * @author Daniel Fern&aacute;ndez
 * @see ISpringWebFluxTemplateEngine
 * @see org.thymeleaf.spring6.SpringWebFluxTemplateEngine
 * @since 2.0.0
 */
public class SpringWebFluxTemplateEngine extends SpringTemplateEngine
    implements ISpringWebFluxTemplateEngine {

    private static final Logger logger = LoggerFactory.getLogger(SpringWebFluxTemplateEngine.class);
    private static final String LOG_CATEGORY_FULL_OUTPUT =
        SpringWebFluxTemplateEngine.class.getName() + ".DOWNSTREAM.FULL";
    private static final String LOG_CATEGORY_CHUNKED_OUTPUT =
        SpringWebFluxTemplateEngine.class.getName() + ".DOWNSTREAM.CHUNKED";
    private static final String LOG_CATEGORY_DATADRIVEN_INPUT =
        SpringWebFluxTemplateEngine.class.getName() + ".UPSTREAM.DATA-DRIVEN";
    private static final String LOG_CATEGORY_DATADRIVEN_OUTPUT =
        SpringWebFluxTemplateEngine.class.getName() + ".DOWNSTREAM.DATA-DRIVEN";


    public SpringWebFluxTemplateEngine() {
        super();
    }


    @Override
    public Publisher<DataBuffer> processStream(
        final String template, final Set<String> markupSelectors, final IContext context,
        final DataBufferFactory bufferFactory, final MediaType mediaType, final Charset charset) {
        return processStream(template, markupSelectors, context, bufferFactory, mediaType, charset,
            Integer.MAX_VALUE);
    }


    @Override
    public Publisher<DataBuffer> processStream(
        final String template, final Set<String> markupSelectors, final IContext context,
        final DataBufferFactory bufferFactory, final MediaType mediaType, final Charset charset,
        final int responseMaxChunkSizeBytes) {

        /*
         * PERFORM VALIDATIONS
         */
        if (template == null) {
            return Flux.error(new IllegalArgumentException("Template cannot be null"));
        }
        if (context == null) {
            return Flux.error(new IllegalArgumentException("Context cannot be null"));
        }
        if (bufferFactory == null) {
            return Flux.error(new IllegalArgumentException("Buffer Factory cannot be null"));
        }
        if (mediaType == null) {
            return Flux.error(new IllegalArgumentException("Media Type cannot be null"));
        }
        if (charset == null) {
            return Flux.error(new IllegalArgumentException("Charset cannot be null"));
        }

        if (responseMaxChunkSizeBytes == 0) {
            return Flux.error(new IllegalArgumentException("Max Chunk Size cannot be zero"));
        }

        // Normalize the chunk size in bytes (MAX_VALUE == no limit)
        final int chunkSizeBytes =
            (responseMaxChunkSizeBytes < 0 ? Integer.MAX_VALUE : responseMaxChunkSizeBytes);

        // Determine whether we have been asked to return data as SSE (Server-Sent Events)
        final boolean sse = MediaType.TEXT_EVENT_STREAM.includes(mediaType);

        /*
         * CHECK FOR DATA-DRIVEN EXECUTION
         */
        try {
            final String dataDriverVariableName = findDataDriverInModel(context);
            if (dataDriverVariableName != null) {
                // We should be executing in data-driven mode
                return createDataDrivenStream(
                    template, markupSelectors, context, dataDriverVariableName, bufferFactory,
                    charset, chunkSizeBytes, sse)
                    // We need to subscribe on a new thread to support blocking operations
                    .subscribeOn(Schedulers.boundedElastic());
            }
        } catch (final Throwable t) {
            return Flux.error(t);
        }

        // Check if we need to fail here: If SSE has been requested, a data-driver variable is
        // mandatory
        if (sse) {
            return Flux.error(new TemplateProcessingException(
                "SSE mode has been requested ('Accept: text/event-stream') but no data-driver "
                    + "variable has been "
                    +
                    "added to the model/context. In order to perform SSE rendering, a variable "
                    + "implementing the "
                    +
                    IReactiveDataDriverContextVariable.class.getName()
                    + " interface is required."));
        }

        /*
         * IS THERE A LIMIT IN BUFFER SIZE? if not, given we are not data-driven, we should
         * switch to FULL
         */
        if (chunkSizeBytes == Integer.MAX_VALUE) {
            // No limit on buffer size, so there is no reason to throttle: using FULL mode instead.
            return createFullStream(template, markupSelectors, context, bufferFactory, charset)
                // We need to subscribe on a new thread to support blocking operations
                .subscribeOn(Schedulers.boundedElastic());
        }

        /*
         * CREATE A CHUNKED STREAM
         */
        return createChunkedStream(
            template, markupSelectors, context, bufferFactory, charset, responseMaxChunkSizeBytes)
            // We need to subscribe on a new thread to support blocking operations
            .subscribeOn(Schedulers.boundedElastic());

    }


    private Mono<DataBuffer> createFullStream(
        final String templateName, final Set<String> markupSelectors, final IContext context,
        final DataBufferFactory bufferFactory, final Charset charset) {

        final Mono<DataBuffer> stream =
            Mono.create(
                subscriber -> {

                    if (logger.isTraceEnabled()) {
                        logger.trace(
                            "[THYMELEAF][{}] STARTING STREAM PROCESS (FULL MODE) OF TEMPLATE "
                                + "\"{}\" WITH LOCALE {}",
                            new Object[] {TemplateEngine.threadIndex(),
                                LoggingUtils.loggifyTemplateName(templateName),
                                context.getLocale()});
                    }

                    final DataBuffer dataBuffer = bufferFactory.allocateBuffer();
                    // OutputStreamWriter object have an 8K buffer, but process(...) will flush
                    // it at the end
                    final OutputStreamWriter
                        writer = new OutputStreamWriter(dataBuffer.asOutputStream(), charset);

                    try {

                        process(templateName, markupSelectors, context, writer);

                    } catch (final Throwable t) {
                        logger.error(
                            String.format(
                                "[THYMELEAF][%s] Exception processing template \"%s\": %s",
                                new Object[] {TemplateEngine.threadIndex(),
                                    LoggingUtils.loggifyTemplateName(templateName),
                                    t.getMessage()}),
                            t);
                        subscriber.error(t);
                        return;
                    }

                    final int bytesProduced = dataBuffer.readableByteCount();

                    if (logger.isTraceEnabled()) {
                        logger.trace(
                            "[THYMELEAF][{}] FINISHED STREAM PROCESS (FULL MODE) OF TEMPLATE "
                                + "\"{}\" WITH LOCALE {}. PRODUCED {} BYTES",
                            new Object[] {
                                TemplateEngine.threadIndex(),
                                LoggingUtils.loggifyTemplateName(templateName),
                                context.getLocale(), Integer.valueOf(bytesProduced)});
                    }

                    // This is a Mono<?>, so no need to call "next()" or "complete()"
                    subscriber.success(dataBuffer);

                });

        // Will add some logging to the data stream
        return stream.log(LOG_CATEGORY_FULL_OUTPUT, Level.FINEST)
            .subscribeOn(Schedulers.boundedElastic());

    }


    private Flux<DataBuffer> createChunkedStream(
        final String templateName, final Set<String> markupSelectors, final IContext context,
        final DataBufferFactory bufferFactory, final Charset charset,
        final int responseMaxChunkSizeBytes) {

        final Flux<DataBuffer> stream = Flux.generate(

            // Using the throttledProcessor as state in this Flux.generate allows us to delay the
            // initialization of the throttled processor until the last moment, when output
            // generation
            // is really requested.
            // NOTE 'sse' is specified as 'false' because SSE is only allowed in data-driven mode
            // . Also, no
            // data-driven iterator is available (we are in chunked mode).
            () -> new SpringWebFluxTemplateEngine.StreamThrottledTemplateProcessor(
                processThrottled(templateName, markupSelectors, context), null, null, 0L, false),

            // This stream will execute, in a one-by-one (non-interleaved) fashion, the following
            // code
            // for each back-pressure request coming from downstream. Each of these steps
            // (chunks) will
            // execute the throttled processor once and return its result as a DataBuffer object.
            (throttledProcessor, emitter) -> {

                throttledProcessor.startChunk();

                if (logger.isTraceEnabled()) {
                    logger.trace(
                        "[THYMELEAF][{}][{}] STARTING PARTIAL STREAM PROCESS (CHUNKED MODE, "
                            + "THROTTLER ID "
                            +
                            "\"{}\", CHUNK {}) FOR TEMPLATE \"{}\" WITH LOCALE {}",
                        new Object[] {
                            TemplateEngine.threadIndex(),
                            throttledProcessor.getProcessorIdentifier(),
                            throttledProcessor.getProcessorIdentifier(),
                            Integer.valueOf(throttledProcessor.getChunkCount()),
                            LoggingUtils.loggifyTemplateName(templateName), context.getLocale()});
                }

                final DataBuffer buffer = bufferFactory.allocateBuffer(responseMaxChunkSizeBytes);

                final int bytesProduced;
                try {
                    bytesProduced =
                        throttledProcessor.process(responseMaxChunkSizeBytes,
                            buffer.asOutputStream(), charset);
                } catch (final Throwable t) {
                    emitter.error(t);
                    return null;
                }

                if (logger.isTraceEnabled()) {
                    logger.trace(
                        "[THYMELEAF][{}][{}] FINISHED PARTIAL STREAM PROCESS (CHUNKED MODE, "
                            + "THROTTLER ID "
                            +
                            "\"{}\", CHUNK {}) FOR TEMPLATE \"{}\" WITH LOCALE {}. PRODUCED {} "
                            + "BYTES",
                        new Object[] {
                            TemplateEngine.threadIndex(),
                            throttledProcessor.getProcessorIdentifier(),
                            throttledProcessor.getProcessorIdentifier(),
                            Integer.valueOf(throttledProcessor.getChunkCount()),
                            LoggingUtils.loggifyTemplateName(templateName), context.getLocale(),
                            Integer.valueOf(bytesProduced)});
                }

                emitter.next(buffer);

                if (throttledProcessor.isFinished()) {

                    if (logger.isTraceEnabled()) {
                        logger.trace(
                            "[THYMELEAF][{}][{}] FINISHED ALL STREAM PROCESS (CHUNKED MODE, "
                                + "THROTTLER ID "
                                +
                                "\"{}\") FOR TEMPLATE \"{}\" WITH LOCALE {}. PRODUCED A TOTAL OF "
                                + "{} BYTES IN {} CHUNKS",
                            new Object[] {
                                TemplateEngine.threadIndex(),
                                throttledProcessor.getProcessorIdentifier(),
                                throttledProcessor.getProcessorIdentifier(),
                                LoggingUtils.loggifyTemplateName(templateName), context.getLocale(),
                                Long.valueOf(throttledProcessor.getTotalBytesProduced()),
                                Integer.valueOf(throttledProcessor.getChunkCount() + 1)});
                    }

                    emitter.complete();

                }

                return throttledProcessor;

            });

        // Will add some logging to the data stream
        return stream.log(LOG_CATEGORY_CHUNKED_OUTPUT, Level.FINEST);

    }


    private Flux<DataBuffer> createDataDrivenStream(
        final String templateName, final Set<String> markupSelectors, final IContext context,
        final String dataDriverVariableName, final DataBufferFactory bufferFactory,
        final Charset charset,
        final int responseMaxChunkSizeBytes, final boolean sse) {

        // STEP 1: Obtain the data-driver variable and its metadata
        final IReactiveDataDriverContextVariable dataDriver =
            (IReactiveDataDriverContextVariable) context.getVariable(dataDriverVariableName);
        final int bufferSizeElements = dataDriver.getBufferSizeElements();
        final String sseEventsPrefix =
            (dataDriver instanceof IReactiveSSEDataDriverContextVariable
                ? ((IReactiveSSEDataDriverContextVariable) dataDriver).getSseEventsPrefix() : null);
        final long sseEventsID =
            (dataDriver instanceof IReactiveSSEDataDriverContextVariable
                ? ((IReactiveSSEDataDriverContextVariable) dataDriver).getSseEventsFirstID() : 0L);
        final ReactiveAdapterRegistry reactiveAdapterRegistry;
        if (Contexts.isSpringWebFluxWebContext(context)) {
            reactiveAdapterRegistry =
                Contexts.getSpringWebFluxWebExchange(context).getApplication()
                    .getReactiveAdapterRegistry();
        } else {
            reactiveAdapterRegistry = null;
        }


        // STEP 2: Replace the data driver variable with a DataDrivenTemplateIterator
        final DataDrivenTemplateIterator dataDrivenIterator = new DataDrivenTemplateIterator();
        final IContext wrappedContext =
            applyDataDriverWrapper(context, dataDriverVariableName, dataDrivenIterator);


        // STEP 3: Create the data stream buffers, plus add some logging in order to know how the
        // stream is being used
        final Flux<List<Object>> dataDrivenBufferedStream =
            Flux.from(dataDriver.getDataStream(reactiveAdapterRegistry))
                .buffer(bufferSizeElements)
                .log(LOG_CATEGORY_DATADRIVEN_INPUT, Level.FINEST);


        // STEP 4: Initialize the (throttled) template engine for each subscriber (normally there
        // will only be one)
        final Flux<SpringWebFluxTemplateEngine.DataDrivenFluxStep> dataDrivenWithContextStream =
            Flux.using(

                // Using the throttledProcessor as state in this Flux.using allows us to delay the
                // initialization of the throttled processor until the last moment, when output
                // generation
                // is really requested.
                () -> {
                    final String outputContentType = sse ? "text/event-stream" : null;
                    final TemplateSpec templateSpec =
                        new TemplateSpec(templateName, markupSelectors, outputContentType, null);
                    return new SpringWebFluxTemplateEngine.StreamThrottledTemplateProcessor(
                        processThrottled(templateSpec, wrappedContext), dataDrivenIterator,
                        sseEventsPrefix, sseEventsID, sse);
                },

                // This flux will be made by concatenating a phase for the head (template before
                // data-driven
                // iteration), another phase composed of most possibly several steps for the
                // data-driven iteration,
                // and finally a tail phase (template after data-driven iteration).
                //
                // But this concatenation will be done from a Flux created with Flux.generate, so
                // that we have the
                // opportunity to check if the processor has already signaled that it has
                // finished, and in such
                // case we might be able to avoid the subscription to the upstream data driver if
                // its iteration is
                // not needed at the template.
                throttledProcessor -> Flux.concat(Flux.generate(
                    () -> DATA_DRIVEN_PHASE_HEAD,
                    (phase, emitter) -> {

                        // Check if the processor has already signaled it has finished, in which
                        // case we
                        // might be able to avoid the BUFFER phase (if no iteration of the
                        // data-driver is present).
                        //
                        // *NOTE* we CANNOT GUARANTEE that this will stop the upstream data
                        // driver publisher from
                        // being subscribed to or even consumed, because there is no guarantee
                        // that this code
                        // will be executed for the BUFFER phase after the entire Flux generated
                        // downstream
                        // for the HEAD phase (see STEP 5 in the stream being built). Actually,
                        // it might even be
                        // executed concurrently to one of the steps of a Flux for the
                        // HEAD/BUFFER phases, which
                        // is why the IThrottledProcessor.isFinished() called here needs to be
                        // thread-safe.
                        if (throttledProcessor.isFinished()) {
                            // We can short-cut, and if we are lucky even avoid the BUFFER phase.
                            emitter.complete();
                            return null;
                        }

                        switch (phase) {

                            case DATA_DRIVEN_PHASE_HEAD:
                                emitter.next(Mono.just(
                                    DataDrivenFluxStep.forHead(
                                        throttledProcessor)));
                                return DATA_DRIVEN_PHASE_BUFFER;

                            case DATA_DRIVEN_PHASE_BUFFER:
                                emitter.next(dataDrivenBufferedStream.map(
                                    values -> DataDrivenFluxStep.forBuffer(
                                        throttledProcessor, values)));
                                return DATA_DRIVEN_PHASE_TAIL;

                            case DATA_DRIVEN_PHASE_TAIL:
                                emitter.next(Mono.just(
                                    DataDrivenFluxStep.forTail(
                                        throttledProcessor)));
                                emitter.complete();
                                break;
                            default:
                                return null;
                        }

                        return null;

                    }
                )),

                // No need to explicitly dispose the throttled template processor.
                throttledProcessor -> { /* Nothing to be done here! */ });


        // STEP 5: React to each buffer of published data by creating one or many (concatMap)
        // DataBuffers containing
        //         the result of processing only that buffer.
        final Flux<DataBuffer> stream = dataDrivenWithContextStream.concatMap(
            (step) -> Flux.generate(

                // We set initialize to TRUE as a state, so that the first step executed for this
                // Flux
                // performs the initialization of the dataDrivenIterator for the entire Flux. It
                // is a need
                // that this initialization is performed when the first step of this Flux is
                // executed,
                // because initialization actually consists of a lateral effect on a mutable
                // variable
                // (the dataDrivenIterator). And this way we are certain that it is executed in the
                // right order, given concatMap guarantees to us that these Fluxes generated here
                // will
                // be consumed in the right order and executed one at a time (and the Reactor
                // guarantees us
                // that there will be no thread visibility issues between Flux steps).
                () -> Boolean.TRUE,

                // The first time this is executed, initialize will be TRUE. From then on, it
                // will be FALSE
                // so that it is the first execution of this that initializes the (mutable)
                // dataDrivenIterator.
                (initialize, emitter) -> {

                    final SpringWebFluxTemplateEngine.StreamThrottledTemplateProcessor
                        throttledProcessor = step.getThrottledProcessor();
                    final DataDrivenTemplateIterator dataDrivenTemplateIterator =
                        throttledProcessor.getDataDrivenTemplateIterator();

                    // Let's check if we can short cut and simply finish execution. Maybe we can
                    // avoid consuming
                    // the data from the upstream data-driver publisher (e.g. if the data-driver
                    // variable is
                    // never actually iterated).
                    if (throttledProcessor.isFinished()) {
                        emitter.complete();
                        return Boolean.FALSE;
                    }

                    // Initialize the dataDrivenIterator. This is a lateral effect, this variable
                    // is mutable,
                    // so it is important to do it here so that we make sure it is executed in
                    // the right order.
                    if (initialize.booleanValue()) {

                        if (step.isHead()) {
                            // Feed with no elements - we just want to output the part of the
                            // template that goes before the iteration of the data driver.
                            dataDrivenTemplateIterator.startHead();
                        } else if (step.isDataBuffer()) {
                            // Value-based execution: we have values and we want to iterate them
                            dataDrivenTemplateIterator.feedBuffer(step.getValues());
                        } else { // step.isTail()
                            // Signal feeding complete, indicating this is just meant to output the
                            // rest of the template after the iteration of the data driver. Note
                            // there
                            // is a case when this phase will still provoke the output of an
                            // iteration,
                            // and this is when the number of iterations is exactly ONE. In this
                            // case,
                            // it won't be possible to determine the iteration type (ZERO, ONE,
                            // MULTIPLE)
                            // until we close it with this 'feedingComplete()'
                            dataDrivenTemplateIterator.feedingComplete();
                            dataDrivenTemplateIterator.startTail();
                        }

                    }

                    // Signal the start of a new chunk (we are counting them for the logs)
                    throttledProcessor.startChunk();

                    if (logger.isTraceEnabled()) {
                        logger.trace(
                            "[THYMELEAF][{}][{}] STARTING PARTIAL STREAM PROCESS (DATA-DRIVEN "
                                + "MODE, THROTTLER ID "
                                +
                                "\"{}\", CHUNK {}) FOR TEMPLATE \"{}\" WITH LOCALE {}",
                            new Object[] {
                                TemplateEngine.threadIndex(),
                                throttledProcessor.getProcessorIdentifier(),
                                throttledProcessor.getProcessorIdentifier(),
                                Integer.valueOf(throttledProcessor.getChunkCount()),
                                LoggingUtils.loggifyTemplateName(templateName),
                                context.getLocale()});
                    }

                    final DataBuffer buffer =
                        (responseMaxChunkSizeBytes != Integer.MAX_VALUE
                            ? bufferFactory.allocateBuffer(responseMaxChunkSizeBytes) :
                            bufferFactory.allocateBuffer());

                    final int bytesProduced;
                    try {

                        bytesProduced =
                            throttledProcessor.process(responseMaxChunkSizeBytes,
                                buffer.asOutputStream(), charset);

                    } catch (final Throwable t) {
                        emitter.error(t);
                        return Boolean.FALSE;
                    }


                    if (logger.isTraceEnabled()) {
                        logger.trace(
                            "[THYMELEAF][{}][{}] FINISHED PARTIAL STREAM PROCESS (DATA-DRIVEN "
                                + "MODE, THROTTLER ID "
                                +
                                "\"{}\", CHUNK {}) FOR TEMPLATE \"{}\" WITH LOCALE {}. PRODUCED "
                                + "{} BYTES",
                            new Object[] {
                                TemplateEngine.threadIndex(),
                                throttledProcessor.getProcessorIdentifier(),
                                throttledProcessor.getProcessorIdentifier(),
                                Integer.valueOf(throttledProcessor.getChunkCount()),
                                LoggingUtils.loggifyTemplateName(templateName), context.getLocale(),
                                Integer.valueOf(bytesProduced)});
                    }


                    // If we produced no bytes, then let's avoid skipping an event number from
                    // the sequence
                    if (bytesProduced == 0) {
                        dataDrivenTemplateIterator.takeBackLastEventID();
                    }


                    // Now it's time to determine if we should execute another time for the same
                    // data-driven step or rather we should consider we have done everything
                    // possible
                    // for this step (e.g. produced all markup for a data stream buffer) and just
                    // emit "complete" and go for the next step.
                    boolean phaseFinished = false;
                    if (throttledProcessor.isFinished()) {

                        if (logger.isTraceEnabled()) {
                            logger.trace(
                                "[THYMELEAF][{}][{}] FINISHED ALL STREAM PROCESS (DATA-DRIVEN "
                                    + "MODE, THROTTLER ID "
                                    +
                                    "\"{}\") FOR TEMPLATE \"{}\" WITH LOCALE {}. PRODUCED A TOTAL"
                                    + " OF {} BYTES IN {} CHUNKS",
                                new Object[] {
                                    TemplateEngine.threadIndex(),
                                    throttledProcessor.getProcessorIdentifier(),
                                    throttledProcessor.getProcessorIdentifier(),
                                    LoggingUtils.loggifyTemplateName(templateName),
                                    context.getLocale(),
                                    Long.valueOf(throttledProcessor.getTotalBytesProduced()),
                                    Integer.valueOf(throttledProcessor.getChunkCount() + 1)});
                        }

                        // We have finished executing the template, which can happen after
                        // finishing iterating all data driver values, or also if we are at the
                        // first execution and there was no need to use the data driver at all
                        phaseFinished = true;
                        dataDrivenTemplateIterator.finishStep();

                    } else {

                        if (step.isHead() && dataDrivenTemplateIterator.hasBeenQueried()) {

                            // We know everything before the data driven iteration has already been
                            // processed because the iterator has been used at least once (i.e. its
                            // 'hasNext()' or 'next()' method have been called at least once).
                            // This will
                            // mean we can switch to the buffer phase.
                            phaseFinished = true;
                            dataDrivenTemplateIterator.finishStep();

                        } else if (step.isDataBuffer()
                            && !dataDrivenTemplateIterator.continueBufferExecution()) {
                            // We have finished executing this buffer of items and we can go for the
                            // next one or maybe the tail.
                            phaseFinished = true;
                        }
                        // fluxStep.isTail(): nothing to do, as the only reason we would have to
                        // emit
                        // 'complete' at the tail step would be throttledProcessor.isFinished(),
                        // which
                        // has been already checked.

                    }

                    // Compute if the output for this step has been already finished (i.e. not
                    // only the
                    // processing of the model's events, but also any existing overflows). This
                    // has to be
                    // queried BEFORE the buffer is emitted.
                    final boolean stepOutputFinished =
                        dataDrivenTemplateIterator.isStepOutputFinished();

                    // Buffer has now everything it should, so send it to the output channels
                    emitter.next(buffer);


                    // If step finished, we have ot emit 'complete' now, giving the opportunity
                    // to execute
                    // again if processing has finished, but we still have some overflow to be
                    // flushed
                    if (phaseFinished && stepOutputFinished) {
                        emitter.complete();
                    }


                    return Boolean.FALSE;

                }));


        // Will add some logging to the data flow
        return stream.log(LOG_CATEGORY_DATADRIVEN_OUTPUT, Level.FINEST);

    }


    /*
     * This method will apply a wrapper on the data driver variable so that a
     * DataDrivenTemplateIterator takes
     * the place of the original data-driver variable. This is done via a wrapper in order to not
     *  perform such a
     * strong modification on the original context object. Even if context objects should not be
     * reused among template
     * engine executions, when a non-IEngineContext implementation is used we will let that
     * degree of liberty to the
     * user just in case.
     */
    private static IContext applyDataDriverWrapper(
        final IContext context, final String dataDriverVariableName,
        final DataDrivenTemplateIterator dataDrivenTemplateIterator) {

        // This is an IEngineContext, a very internal, low-level context implementation, so let's
        // simply modify it
        if (context instanceof IEngineContext) {
            ((IEngineContext) context).setVariable(dataDriverVariableName,
                dataDrivenTemplateIterator);
            return context;
        }

        // Not an IEngineContext, but might still be an IWebContext, and we don't want to lose
        // that info
        if (Contexts.isWebContext(context)) {
            return new SpringWebFluxTemplateEngine.DataDrivenWebContextWrapper(
                Contexts.asWebContext(context), dataDriverVariableName, dataDrivenTemplateIterator);
        }

        // Not a recognized context interface: just use a default implementation
        return new SpringWebFluxTemplateEngine.DataDrivenContextWrapper(context,
            dataDriverVariableName, dataDrivenTemplateIterator);


    }


    private static String findDataDriverInModel(final IContext context) {

        // In SpringWebFluxContext (used most of the times), variables are backed by a
        // Map<String,Object>. So this iteration on all the names and many "getVariable()" calls
        // shouldn't be an issue perf-wise.

        String dataDriverVariableName = null;
        final Set<String> contextVariableNames = context.getVariableNames();

        for (final String contextVariableName : contextVariableNames) {

            final Object contextVariableValue = context.getVariable(contextVariableName);
            if (contextVariableValue instanceof IReactiveDataDriverContextVariable) {
                if (dataDriverVariableName != null) {
                    throw new TemplateProcessingException(
                        "Only one data-driver variable is allowed to be specified as a model "
                            + "attribute, but "
                            + "at least two have been identified: '" + dataDriverVariableName + "' "
                            + "and '" + contextVariableName + "'");
                }
                dataDriverVariableName = contextVariableName;
            }

        }

        return dataDriverVariableName;

    }


    /*
     * This internal class is meant to be used in multi-step streams so that an account on the total
     * number of bytes and steps/chunks can be kept, and also other aspects such as SSE event
     * management can be offered.
     *
     * NOTE there is no need to synchronize these variables, even if different steps/chunks might
     *  be executed
     * (non-concurrently) by different threads, because Reactive Streams implementations like
     * Reactor should
     * take care to establish the adequate thread synchronization/memory barriers at their
     * asynchronous boundaries,
     * thus avoiding thread visibility issues.
     */
    static class StreamThrottledTemplateProcessor {

        private final IThrottledTemplateProcessor throttledProcessor;
        private final DataDrivenTemplateIterator dataDrivenTemplateIterator;
        private int chunkCount;
        private long totalBytesProduced;

        StreamThrottledTemplateProcessor(
            final IThrottledTemplateProcessor throttledProcessor,
            final DataDrivenTemplateIterator dataDrivenTemplateIterator,
            final String sseEventsPrefix, final long sseEventsFirstID, final boolean sse) {

            super();

            this.throttledProcessor = throttledProcessor;
            this.dataDrivenTemplateIterator = dataDrivenTemplateIterator;

            final IThrottledTemplateWriterControl writerControl;
            if (this.throttledProcessor instanceof ThrottledTemplateProcessor) {
                writerControl = ((ThrottledTemplateProcessor)
                    this.throttledProcessor).getThrottledTemplateWriterControl();
            } else {
                writerControl = null;
            }

            if (sse) {
                if (writerControl == null
                    || !(writerControl instanceof ISSEThrottledTemplateWriterControl)) {
                    throw new TemplateProcessingException(
                        "Cannot process template in Server-Sent Events (SSE) mode: template "
                            + "writer is not SSE capable. "
                            +
                            "Either SSE content type has not been declared at the "
                            + TemplateSpec.class.getSimpleName() + " or "
                            + "an implementation of " + IThrottledTemplateProcessor.class.getName()
                            + " other than "
                            + ThrottledTemplateProcessor.class.getName() + " is being used.");
                }
                if (this.dataDrivenTemplateIterator == null) {
                    throw new TemplateProcessingException(
                        "Cannot process template in Server-Sent Events (SSE) mode: a data-driven "
                            + "template iterator "
                            + "is required in context in order to apply SSE.");
                }
            }

            if (this.dataDrivenTemplateIterator != null) {
                this.dataDrivenTemplateIterator.setWriterControl(writerControl);
                this.dataDrivenTemplateIterator.setSseEventsPrefix(sseEventsPrefix);
                this.dataDrivenTemplateIterator.setSseEventsFirstID(sseEventsFirstID);
            }

            this.chunkCount = -1; // First chunk will be considered number 0
            this.totalBytesProduced = 0L;

        }

        int process(final int maxOutputInBytes, final OutputStream outputStream,
            final Charset charset) {
            final int chunkBytes =
                this.throttledProcessor.process(maxOutputInBytes, outputStream, charset);
            this.totalBytesProduced += chunkBytes;
            return chunkBytes;
        }

        String getProcessorIdentifier() {
            return this.throttledProcessor.getProcessorIdentifier();
        }

        boolean isFinished() {
            return this.throttledProcessor.isFinished();
        }

        void startChunk() {
            this.chunkCount++;
        }

        int getChunkCount() {
            return this.chunkCount;
        }

        long getTotalBytesProduced() {
            return this.totalBytesProduced;
        }

        DataDrivenTemplateIterator getDataDrivenTemplateIterator() {
            return this.dataDrivenTemplateIterator;
        }

    }


    /*
     * This internal class is used for keeping the accounting of the different phases in a
     * data-driven stream:
     * head (no value, template before the data-driven iteration), buffer (values, data-driven
     * iteration), and
     * tail (no value, template after the data-driven iteration).
     *
     * NOTE there is no need to synchronize these variables, even if different steps/chunks might
     *  be executed
     * (non-concurrently) by different threads, because Reactive Streams implementations like
     * Reactor should
     * take care to establish the adequate thread synchronization/memory barriers at their
     * asynchronous boundaries,
     * thus avoiding thread visibility issues.
     */
    static final class DataDrivenFluxStep {

        enum FluxStepPhase {
            DATA_DRIVEN_PHASE_HEAD, DATA_DRIVEN_PHASE_BUFFER, DATA_DRIVEN_PHASE_TAIL
        }

        private final SpringWebFluxTemplateEngine.StreamThrottledTemplateProcessor
            throttledProcessor;
        private final List<Object> values;
        private final SpringWebFluxTemplateEngine.DataDrivenFluxStep.FluxStepPhase phase;


        static SpringWebFluxTemplateEngine.DataDrivenFluxStep forHead(
            final SpringWebFluxTemplateEngine.StreamThrottledTemplateProcessor throttledProcessor) {
            return new SpringWebFluxTemplateEngine.DataDrivenFluxStep(throttledProcessor, null,
                DATA_DRIVEN_PHASE_HEAD);
        }

        static SpringWebFluxTemplateEngine.DataDrivenFluxStep forBuffer(
            final SpringWebFluxTemplateEngine.StreamThrottledTemplateProcessor throttledProcessor,
            final List<Object> values) {
            return new SpringWebFluxTemplateEngine.DataDrivenFluxStep(throttledProcessor, values,
                DATA_DRIVEN_PHASE_BUFFER);
        }

        static SpringWebFluxTemplateEngine.DataDrivenFluxStep forTail(
            final SpringWebFluxTemplateEngine.StreamThrottledTemplateProcessor throttledProcessor) {
            return new SpringWebFluxTemplateEngine.DataDrivenFluxStep(throttledProcessor, null,
                DATA_DRIVEN_PHASE_TAIL);
        }

        private DataDrivenFluxStep(
            final SpringWebFluxTemplateEngine.StreamThrottledTemplateProcessor throttledProcessor,
            final List<Object> values,
            final SpringWebFluxTemplateEngine.DataDrivenFluxStep.FluxStepPhase phase) {
            super();
            this.throttledProcessor = throttledProcessor;
            this.values = values;
            this.phase = phase;
        }

        SpringWebFluxTemplateEngine.StreamThrottledTemplateProcessor getThrottledProcessor() {
            return this.throttledProcessor;
        }

        List<Object> getValues() {
            return this.values;
        }

        boolean isHead() {
            return this.phase == DATA_DRIVEN_PHASE_HEAD;
        }

        boolean isDataBuffer() {
            return this.phase == DATA_DRIVEN_PHASE_BUFFER;
        }

        boolean isTail() {
            return this.phase == DATA_DRIVEN_PHASE_TAIL;
        }

    }


    /*
     * This wrapper of an IWebContext is meant to wrap the original context object sent to the
     * template engine while hiding the data driver variable, returning a
     * DataDrivenTemplateIterator in its place.
     */
    static class DataDrivenWebContextWrapper
        extends SpringWebFluxTemplateEngine.DataDrivenContextWrapper implements IWebContext {

        private final IWebContext context;

        DataDrivenWebContextWrapper(
            final IWebContext context, final String dataDriverVariableName,
            final DataDrivenTemplateIterator dataDrivenTemplateIterator) {
            super(context, dataDriverVariableName, dataDrivenTemplateIterator);
            this.context = context;
        }

        @Override
        public IWebExchange getExchange() {
            return this.context.getExchange();
        }

    }


    /*
     * This wrapper of an IContext (non-SpringWebFlux-specific) is meant to wrap the original
     * context object sent
     * to the template engine while hiding the data driver variable, returning a
     * DataDrivenTemplateIterator in
     * its place.
     */
    static class DataDrivenContextWrapper implements IContext {

        private final IContext context;
        private final String dataDriverVariableName;
        private final DataDrivenTemplateIterator dataDrivenTemplateIterator;

        DataDrivenContextWrapper(
            final IContext context, final String dataDriverVariableName,
            final DataDrivenTemplateIterator dataDrivenTemplateIterator) {
            super();
            this.context = context;
            this.dataDriverVariableName = dataDriverVariableName;
            this.dataDrivenTemplateIterator = dataDrivenTemplateIterator;
        }

        public IContext getWrappedContext() {
            return this.context;
        }

        @Override
        public Locale getLocale() {
            return this.context.getLocale();
        }

        @Override
        public boolean containsVariable(final String name) {
            return this.context.containsVariable(name);
        }

        @Override
        public Set<String> getVariableNames() {
            return this.context.getVariableNames();
        }

        @Override
        public Object getVariable(final String name) {
            if (this.dataDriverVariableName.equals(name)) {
                return this.dataDrivenTemplateIterator;
            }
            return this.context.getVariable(name);
        }

    }
}