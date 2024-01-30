package run.halo.app.theme.finders;

import java.util.Map;
import org.springframework.context.ApplicationContext;

/**
 * Finder registry for class annotated with {@link Finder}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface FinderRegistry {

    Map<String, Object> getFinders();

    void register(String pluginId, ApplicationContext pluginContext);

    void unregister(String pluginId);

}
