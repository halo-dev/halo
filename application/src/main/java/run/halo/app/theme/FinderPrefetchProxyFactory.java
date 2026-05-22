package run.halo.app.theme;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

public enum FinderPrefetchProxyFactory {
    ;

    public static Object create(String finderName, Object finder) {
        return create(finderName, finder, null, null);
    }

    public static Object create(
            String finderName,
            Object finder,
            @Nullable FinderPrefetchContext prefetchContext,
            @Nullable ContextView contextView) {
        var interfaces = getAllInterfaces(finder.getClass());
        if (interfaces.isEmpty()) {
            return finder;
        }
        return Proxy.newProxyInstance(
                finder.getClass().getClassLoader(),
                interfaces.toArray(Class[]::new),
                new PrefetchInvocationHandler(finderName, finder, prefetchContext, contextView));
    }

    private static Set<Class<?>> getAllInterfaces(Class<?> type) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            interfaces.addAll(Arrays.asList(current.getInterfaces()));
            current = current.getSuperclass();
        }
        return interfaces;
    }

    private record PrefetchInvocationHandler(
            String finderName,
            Object target,
            @Nullable FinderPrefetchContext prefetchContext,
            @Nullable ContextView contextView)
            implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(target, args);
            }
            if (Mono.class.isAssignableFrom(method.getReturnType())) {
                if (prefetchContext != null && contextView != null) {
                    Supplier<Mono<?>> sourceSupplier = () -> (Mono<?>) invokeTarget(method, args);
                    return prefetchContext.resolveMono(
                            new FinderPrefetchContext.InvocationKey(finderName, method, args),
                            contextView,
                            sourceSupplier);
                }
                return Mono.deferContextual(contextView -> {
                    Supplier<Mono<?>> sourceSupplier = () -> (Mono<?>) invokeTarget(method, args);
                    var prefetchContext = contextView.getOrDefault(FinderPrefetchContext.CONTEXT_KEY, null);
                    if (prefetchContext instanceof FinderPrefetchContext finderPrefetchContext) {
                        return finderPrefetchContext.resolveMono(
                                new FinderPrefetchContext.InvocationKey(finderName, method, args),
                                contextView,
                                sourceSupplier);
                    }
                    return sourceSupplier.get();
                });
            }
            if (Flux.class.isAssignableFrom(method.getReturnType())) {
                if (prefetchContext != null && contextView != null) {
                    Supplier<Flux<?>> sourceSupplier = () -> (Flux<?>) invokeTarget(method, args);
                    @SuppressWarnings("unchecked")
                    Publisher<Object> typedPublisher = (Publisher<Object>) prefetchContext.resolveFlux(
                            new FinderPrefetchContext.InvocationKey(finderName, method, args),
                            contextView,
                            sourceSupplier);
                    return Flux.from(typedPublisher);
                }
                return Flux.deferContextual(contextView -> {
                    Supplier<Flux<?>> sourceSupplier = () -> (Flux<?>) invokeTarget(method, args);
                    var prefetchContext = contextView.getOrDefault(FinderPrefetchContext.CONTEXT_KEY, null);
                    Publisher<?> publisher;
                    if (prefetchContext instanceof FinderPrefetchContext finderPrefetchContext) {
                        publisher = finderPrefetchContext.resolveFlux(
                                new FinderPrefetchContext.InvocationKey(finderName, method, args),
                                contextView,
                                sourceSupplier);
                    } else {
                        publisher = sourceSupplier.get();
                    }
                    @SuppressWarnings("unchecked")
                    Publisher<Object> typedPublisher = (Publisher<Object>) publisher;
                    return typedPublisher;
                });
            }
            if (Publisher.class.isAssignableFrom(method.getReturnType())) {
                return Mono.deferContextual(contextView -> Mono.from((Publisher<?>) invokeTarget(method, args)));
            }
            return invokeTarget(method, args);
        }

        private Object invokeTarget(Method method, Object[] args) {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw Exceptions.propagate(e.getTargetException());
            } catch (ReflectiveOperationException e) {
                throw Exceptions.propagate(e);
            }
        }
    }
}
