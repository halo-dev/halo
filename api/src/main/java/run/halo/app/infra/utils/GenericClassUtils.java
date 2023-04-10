package run.halo.app.infra.utils;

import java.io.IOException;
import java.util.function.Supplier;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import reactor.core.Exceptions;

public enum GenericClassUtils {
    ;

    /**
     * Generate concrete class of generic class. e.g.: {@code List<String>}
     *
     * @param rawClass is generic class, like {@code List.class}
     * @param parameterType is parameter type of generic class
     * @param <T> parameter type
     * @return generated class
     */
    public static <T> Class<?> generateConcreteClass(Class<?> rawClass, Class<T> parameterType) {
        return generateConcreteClass(rawClass, parameterType, () ->
            parameterType.getName() + rawClass.getSimpleName());
    }

    /**
     * Generate concrete class of generic class. e.g.: {@code List<String>}
     *
     * @param rawClass is generic class, like {@code List.class}
     * @param parameterType is parameter type of generic class
     * @param nameGenerator is generated class name
     * @param <T> parameter type
     * @return generated class
     */
    public static <T> Class<?> generateConcreteClass(Class<?> rawClass, Class<T> parameterType,
        Supplier<String> nameGenerator) {
        var concreteType =
            TypeDescription.Generic.Builder.parameterizedType(rawClass, parameterType).build();
        try (var unloaded = new ByteBuddy()
            .subclass(concreteType)
            .name(nameGenerator.get())
            .make()) {
            return unloaded.load(parameterType.getClassLoader()).getLoaded();
        } catch (IOException e) {
            // Should never happen
            throw Exceptions.propagate(e);
        }
    }
}
