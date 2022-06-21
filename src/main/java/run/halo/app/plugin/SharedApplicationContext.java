package run.halo.app.plugin;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * <p>An {@link ApplicationContext} implementation shared by plugins.</p>
 * <p>Beans in the Core that need to be shared with plugins will be injected into this
 * {@link SharedApplicationContext}.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class SharedApplicationContext extends GenericApplicationContext {
}
