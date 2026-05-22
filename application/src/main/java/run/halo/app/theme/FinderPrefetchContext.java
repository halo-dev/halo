package run.halo.app.theme;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.thymeleaf.context.IContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

/** Per-render cache for Finder calls discovered before the final template pass. */
public class FinderPrefetchContext {

    public static final Class<FinderPrefetchContext> CONTEXT_KEY = FinderPrefetchContext.class;

    private final Map<InvocationKey, PrefetchedValue> values = new ConcurrentHashMap<>();

    private volatile Phase phase = Phase.RENDER;

    public static Optional<FinderPrefetchContext> from(IContext context, String contextViewKey) {
        return contextViewFrom(context, contextViewKey)
                .filter(contextView -> contextView.hasKey(CONTEXT_KEY))
                .map(contextView -> contextView.get(CONTEXT_KEY));
    }

    public static Optional<ContextView> contextViewFrom(IContext context, String contextViewKey) {
        return Optional.ofNullable(context.getVariable(contextViewKey))
                .filter(ContextView.class::isInstance)
                .map(ContextView.class::cast);
    }

    public Mono<?> resolveMono(InvocationKey key, ContextView contextView, Supplier<Mono<?>> sourceSupplier) {
        if (phase == Phase.DISCOVERY) {
            values.computeIfAbsent(key, ignored -> new PrefetchedMono(contextView, sourceSupplier));
            return Mono.empty();
        }
        var value = values.computeIfAbsent(key, ignored -> new PrefetchedMono(contextView, sourceSupplier));
        if (value instanceof PrefetchedMono prefetchedMono) {
            return prefetchedMono.mono();
        }
        return Mono.defer(sourceSupplier);
    }

    public Flux<?> resolveFlux(InvocationKey key, ContextView contextView, Supplier<Flux<?>> sourceSupplier) {
        if (phase == Phase.DISCOVERY) {
            values.computeIfAbsent(key, ignored -> new PrefetchedFlux(contextView, sourceSupplier));
            return Flux.empty();
        }
        var value = values.computeIfAbsent(key, ignored -> new PrefetchedFlux(contextView, sourceSupplier));
        if (value instanceof PrefetchedFlux prefetchedFlux) {
            return prefetchedFlux.flux();
        }
        return sourceSupplier.get();
    }

    public void startDiscovery() {
        values.clear();
        phase = Phase.DISCOVERY;
    }

    public Mono<Void> awaitPrefetchedValues() {
        return Mono.defer(() -> {
            phase = Phase.PREFETCH;
            return Flux.fromIterable(values.values())
                    .flatMap(PrefetchedValue::prefetch)
                    .then();
        });
    }

    public void startRender() {
        phase = Phase.RENDER;
    }

    public void finish() {
        phase = Phase.RENDER;
        values.clear();
    }

    public record InvocationKey(String finderName, Method method, List<Object> arguments) {
        public InvocationKey(String finderName, Method method, @Nullable Object[] arguments) {
            this(finderName, method, freezeArguments(arguments));
        }

        private static List<Object> freezeArguments(@Nullable Object[] arguments) {
            if (arguments == null || arguments.length == 0) {
                return List.of();
            }
            return Arrays.stream(arguments).map(InvocationKey::freezeArgument).toList();
        }

        private static Object freezeArgument(@Nullable Object argument) {
            if (argument == null
                    || argument instanceof String
                    || argument instanceof Number
                    || argument instanceof Boolean
                    || argument instanceof Enum<?>
                    || argument instanceof Class<?>) {
                return argument;
            }
            if (argument instanceof Map<?, ?> map) {
                Map<Object, Object> frozen = new LinkedHashMap<>();
                map.forEach((key, value) -> frozen.put(freezeArgument(key), freezeArgument(value)));
                return Collections.unmodifiableMap(frozen);
            }
            if (argument instanceof Collection<?> collection) {
                return collection.stream().map(InvocationKey::freezeArgument).toList();
            }
            if (argument.getClass().isArray()) {
                int length = java.lang.reflect.Array.getLength(argument);
                List<Object> frozen = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    frozen.add(freezeArgument(java.lang.reflect.Array.get(argument, i)));
                }
                return List.copyOf(frozen);
            }
            return argument;
        }
    }

    private enum Phase {
        DISCOVERY,
        PREFETCH,
        RENDER
    }

    private interface PrefetchedValue {
        Mono<Void> prefetch();
    }

    private static class PrefetchedMono implements PrefetchedValue {
        private final ContextView contextView;
        private final Supplier<Mono<?>> sourceSupplier;
        private volatile Mono<?> cached;

        PrefetchedMono(ContextView contextView, Supplier<Mono<?>> sourceSupplier) {
            this.contextView = contextView;
            this.sourceSupplier = sourceSupplier;
        }

        @Override
        public Mono<Void> prefetch() {
            return mono().then();
        }

        Mono<?> mono() {
            var current = cached;
            if (current != null) {
                return current;
            }
            synchronized (this) {
                if (cached == null) {
                    cached =
                            Mono.defer(sourceSupplier).contextWrite(contextView).cache();
                }
                return cached;
            }
        }
    }

    private static class PrefetchedFlux implements PrefetchedValue {
        private final ContextView contextView;
        private final Supplier<Flux<?>> sourceSupplier;
        private volatile Mono<List<Object>> cached;

        PrefetchedFlux(ContextView contextView, Supplier<Flux<?>> sourceSupplier) {
            this.contextView = contextView;
            this.sourceSupplier = sourceSupplier;
        }

        @Override
        public Mono<Void> prefetch() {
            return list().then();
        }

        Flux<?> flux() {
            return list().flatMapMany(Flux::fromIterable);
        }

        private Mono<List<Object>> list() {
            var current = cached;
            if (current != null) {
                return current;
            }
            synchronized (this) {
                if (cached == null) {
                    cached = Mono.defer(() -> sourceSupplier
                                    .get()
                                    .cast(Object.class)
                                    .collectList()
                                    .map(List::copyOf))
                            .contextWrite(contextView)
                            .cache();
                }
                return cached;
            }
        }
    }
}
