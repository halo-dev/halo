package run.halo.app.plugin;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>It is a symbolic annotation.</p>
 * <p>When the event marked with {@link SharedEvent} annotation is published, it will be
 * broadcast to the application context of the plugin by
 * {@link PluginApplicationEventBridgeDispatcher}.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SharedEvent {
}
