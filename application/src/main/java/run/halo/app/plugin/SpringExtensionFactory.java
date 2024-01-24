package run.halo.app.plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;
import org.pf4j.ExtensionFactory;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

/**
 * <p>Basic implementation of an extension factory.</p>
 * <p>Uses Springs {@link AutowireCapableBeanFactory} to instantiate a given extension class.</p>
 * <p>All kinds of {@link Autowired} are supported (see example below). If no
 * {@link ApplicationContext} is
 * available (this is the case if either the related plugin is not a {@link BasePlugin} or the
 * given plugin manager is not a {@link HaloPluginManager}), standard Java reflection will be used
 * to instantiate an extension.</p>
 * <p>Creates a new extension instance every time a request is done.</p>
 * Example of supported autowire modes:
 * <pre>{@code
 *     @Extension
 *     public class Foo implements ExtensionPoint {
 *
 *         private final Bar bar;       // Constructor injection
 *         private Baz baz;             // Setter injection
 *         @Autowired
 *         private Qux qux;             // Field injection
 *
 *         @Autowired
 *         public Foo(final Bar bar) {
 *             this.bar = bar;
 *         }
 *
 *         @Autowired
 *         public void setBaz(final Baz baz) {
 *             this.baz = baz;
 *         }
 *     }
 * }</pre>
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SpringExtensionFactory implements ExtensionFactory {

    /**
     * The plugin manager is used for retrieving a plugin from a given extension class and as a
     * fallback supplier of an application context.
     */
    protected final PluginManager pluginManager;

    @Override
    @Nullable
    public <T> T create(Class<T> extensionClass) {
        return getPluginApplicationContextBy(extensionClass)
            .map(context -> context.getBean(extensionClass))
            .orElseGet(() -> createWithoutSpring(extensionClass));
    }

    /**
     * Creates an instance of the given class object by using standard Java reflection.
     *
     * @param extensionClass The class annotated with {@code @}{@link Extension}.
     * @param <T> The type for that an instance should be created.
     * @return an instantiated extension.
     * @throws IllegalArgumentException if the given class object has no public constructor.
     * @throws RuntimeException         if the called constructor cannot be instantiated with {@code
     *                                  null}-parameters.
     */
    @SuppressWarnings("unchecked")
    protected <T> T createWithoutSpring(final Class<T> extensionClass)
        throws IllegalArgumentException {
        final Constructor<?> constructor =
            getPublicConstructorWithShortestParameterList(extensionClass)
                // An extension class is required to have at least one public constructor.
                .orElseThrow(
                    () -> new IllegalArgumentException("Extension class '" + nameOf(extensionClass)
                        + "' must have at least one public constructor."));
        try {
            if (log.isTraceEnabled()) {
                log.trace("Instantiate '" + nameOf(extensionClass) + "' by calling '" + constructor
                    + "'with standard Java reflection.");
            }
            // Creating the instance by calling the constructor with null-parameters (if there
            // are any).
            return (T) constructor.newInstance(nullParameters(constructor));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            // If one of these exceptions is thrown it it most likely because of NPE inside the
            // called constructor and
            // not the reflective call itself as we precisely searched for a fitting constructor.
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(
                "Most likely this exception is thrown because the called constructor ("
                    + constructor + ")"
                    + " cannot handle 'null' parameters. Original message was: "
                    + ex.getMessage(), ex);
        }
    }

    private Optional<Constructor<?>> getPublicConstructorWithShortestParameterList(
        final Class<?> extensionClass) {
        return Stream.of(extensionClass.getConstructors())
            .min(Comparator.comparing(Constructor::getParameterCount));
    }

    private Object[] nullParameters(final Constructor<?> constructor) {
        return new Object[constructor.getParameterCount()];
    }

    protected <T> Optional<ApplicationContext> getPluginApplicationContextBy(
        final Class<T> extensionClass) {
        return Optional.ofNullable(this.pluginManager.whichPlugin(extensionClass))
            .map(PluginWrapper::getPlugin)
            .filter(SpringPlugin.class::isInstance)
            .map(plugin -> (SpringPlugin) plugin)
            .map(SpringPlugin::getApplicationContext);
    }

    private <T> String nameOf(final Class<T> clazz) {
        return clazz.getName();
    }
}
