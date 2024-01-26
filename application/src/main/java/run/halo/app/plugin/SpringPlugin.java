package run.halo.app.plugin;

import org.pf4j.Plugin;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import run.halo.app.plugin.event.SpringPluginStartedEvent;
import run.halo.app.plugin.event.SpringPluginStartingEvent;
import run.halo.app.plugin.event.SpringPluginStoppingEvent;

public class SpringPlugin extends Plugin {

    private ApplicationContext context;

    private Plugin delegate;

    private final PluginApplicationContextFactory contextFactory;

    private final PluginContext pluginContext;

    public SpringPlugin(PluginApplicationContextFactory contextFactory,
        PluginContext pluginContext) {
        this.contextFactory = contextFactory;
        this.pluginContext = pluginContext;
    }

    @Override
    public void start() {
        // initialize context
        var pluginId = pluginContext.getName();
        this.context = contextFactory.create(pluginId);

        var pluginOpt = context.getBeanProvider(Plugin.class)
            .stream()
            .findFirst();
        context.publishEvent(new SpringPluginStartingEvent(this, this));
        if (pluginOpt.isPresent()) {
            this.delegate = pluginOpt.get();
            if (this.delegate instanceof BasePlugin basePlugin) {
                basePlugin.setContext(pluginContext);
            }
            this.delegate.start();
        }
        context.publishEvent(new SpringPluginStartedEvent(this, this));
    }

    @Override
    public void stop() {
        if (context != null) {
            context.publishEvent(new SpringPluginStoppingEvent(this, this));
        }
        if (this.delegate != null) {
            this.delegate.stop();
        }
        if (context instanceof ConfigurableApplicationContext configurableContext) {
            configurableContext.close();
        }
        // reset application context
        context = null;
    }

    @Override
    public void delete() {
        if (delegate != null) {
            delegate.delete();
        }
    }

    public ApplicationContext getApplicationContext() {
        return context;
    }

    public PluginContext getPluginContext() {
        return pluginContext;
    }
}
