package run.halo.app.plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;
import org.pf4j.ExtensionFactory;
import org.pf4j.Plugin;
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
public class SpringExtensionFactory implements ExtensionFactory {

    public static final boolean AUTOWIRE_BY_DEFAULT = true;

    /**
     * The plugin manager is used for retrieving a plugin from a given extension class and as a
     * fallback supplier of an application context.
     */
    protected final PluginManager pluginManager;

    /**
     * Indicates if springs autowiring possibilities should be used.
     */
    protected final boolean autowire;

    public SpringExtensionFactory(PluginManager pluginManager) {
        this(pluginManager, AUTOWIRE_BY_DEFAULT);
    }

    public SpringExtensionFactory(final PluginManager pluginManager, final boolean autowire) {
        this.pluginManager = pluginManager;
        this.autowire = autowire;
        if (!autowire) {
            log.warn(
                "Autowiring is disabled although the only reason for existence of this special "
                    + "factory is"
                    +
                    " supporting spring and its application context.");
        }
    }

    @Override
    @Nullable
    public <T> T create(Class<T> extensionClass) {
        if (!this.autowire) {
            log.warn("Create instance of '" + nameOf(extensionClass)
                + "' without using springs possibilities as"
                + " autowiring is disabled.");
            return createWithoutSpring(extensionClass);
        }
        Optional<PluginApplicationContext> contextOptional =
            getPluginApplicationContextBy(extensionClass);
        if (contextOptional.isPresent()) {
            // When the plugin starts, the class has been loaded into the plugin application
            // context,
            // so you only need to get it directly
            PluginApplicationContext pluginApplicationContext = contextOptional.get();
            return pluginApplicationContext.getBean(extensionClass);
        }
        return createWithoutSpring(extensionClass);
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
            log.debug("Instantiate '" + nameOf(extensionClass) + "' by calling '" + constructor
                + "'with standard Java reflection.");
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

    protected <T> Optional<PluginApplicationContext> getPluginApplicationContextBy(
        final Class<T> extensionClass) {
        final Plugin plugin = Optional.ofNullable(this.pluginManager.whichPlugin(extensionClass))
            .map(PluginWrapper::getPlugin)
            .orElse(null);

        final PluginApplicationContext applicationContext;

        if (plugin instanceof BasePlugin) {
            log.debug(
                "  Extension class ' " + nameOf(extensionClass) + "' belongs to halo-plugin '"
                    + nameOf(plugin)
                    + "' and will be autowired by using its application context.");
            applicationContext = ExtensionContextRegistry.getInstance()
                .getByPluginId(plugin.getWrapper().getPluginId());
            return Optional.of(applicationContext);
        } else if (this.pluginManager instanceof HaloPluginManager && plugin != null) {
            log.debug("  Extension class ' " + nameOf(extensionClass)
                + "' belongs to a non halo-plugin (or main application)"
                + " '" + nameOf(plugin)
                + ", but the used Halo plugin-manager is a spring-plugin-manager. Therefore"
                + " the extension class will be autowired by using the managers application "
                + "contexts");
            String pluginId = plugin.getWrapper().getPluginId();
            applicationContext = ((HaloPluginManager) this.pluginManager)
                .getPluginApplicationContext(pluginId);
        } else {
            log.warn("  No application contexts can be used for instantiating extension class '"
                + nameOf(extensionClass) + "'."
                + " This extension neither belongs to a halo-plugin (id: '" + nameOf(plugin)
                + "') nor is the used"
                + " plugin manager a spring-plugin-manager (used manager: '"
                + nameOf(this.pluginManager.getClass()) + "')."
                + " At perspective of PF4J this seems highly uncommon in combination with a factory"
                + " which only reason for existence"
                + " is using spring (and its application context) and should at least be reviewed. "
                + "In fact no autowiring can be"
                + " applied although autowire flag was set to 'true'. Instantiating will fallback "
                + "to standard Java reflection.");
            applicationContext = null;
        }

        return Optional.ofNullable(applicationContext);
    }

    private String nameOf(final Plugin plugin) {
        return Objects.nonNull(plugin)
            ? plugin.getWrapper().getPluginId()
            : "system";
    }

    private <T> String nameOf(final Class<T> clazz) {
        return clazz.getName();
    }
}
