package run.halo.app.plugin;

import org.pf4j.PluginManager;
import org.springframework.context.ApplicationContext;

public interface SpringPluginManager extends PluginManager {

    ApplicationContext getRootContext();

    ApplicationContext getSharedContext();

}
