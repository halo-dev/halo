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
        log.info("Preparing starting plugin {}", pluginContext.getName());
        var pluginId = pluginContext.getName();
        try {
            // initialize context
            this.context = contextFactory.create(pluginId);
            log.info("Application context {} for plugin {} is created", this.context, pluginId);

            var pluginOpt = context.getBeanProvider(Plugin.class)
                .stream()
                .findFirst();
            log.info("Before publishing plugin starting event for plugin {}", pluginId);
            context.publishEvent(new SpringPluginStartingEvent(this, this));
            log.info("After publishing plugin starting event for plugin {}", pluginId);
            if (pluginOpt.isPresent()) {
                this.delegate = pluginOpt.get();
                if (this.delegate instanceof BasePlugin basePlugin) {
                    basePlugin.setContext(pluginContext);
                }
                log.info("Starting {} for plugin {}", this.delegate, pluginId);
                this.delegate.start();
                log.info("Started {} for plugin {}", this.delegate, pluginId);
            }
            log.info("Before publishing plugin started event for plugin {}", pluginId);
            context.publishEvent(new SpringPluginStartedEvent(this, this));
            log.info("After publishing plugin started event for plugin {}", pluginId);
        } catch (Throwable t) {
            // try to stop plugin for cleaning resources if something went wrong
            log.error(
                "Cleaning up plugin resources for plugin {} due to not being able to start plugin.",
                pluginId);
            this.stop();
            // propagate exception to invoker.
            throw t;
        }
    }

    @Override
    public void stop() {
        try {
            if (context != null) {
                log.info("Before publishing plugin stopping event for plugin {}",
                    pluginContext.getName());
                context.publishEvent(new SpringPluginStoppingEvent(this, this));
                log.info("After publishing plugin stopping event for plugin {}",
                    pluginContext.getName());
            }
            if (this.delegate != null) {
                log.info("Stopping {} for plugin {}", this.delegate, pluginContext.getName());
                this.delegate.stop();
                log.info("Stopped {} for plugin {}", this.delegate, pluginContext.getName());
            }
        } finally {
            if (context instanceof ConfigurableApplicationContext configurableContext) {
                log.info("Closing plugin context for plugin {}", pluginContext.getName());
                configurableContext.close();
                log.info("Closed plugin context for plugin {}", pluginContext.getName());
            }
            // reset application context
            log.info("Reset plugin context for plugin {}", pluginContext.getName());
            context = null;
        }
    }

    @Override
    public void delete() {
        if (delegate != null) {
            delegate.delete();
        }
        this.delegate = null;
    }

    public ApplicationContext getApplicationContext() {
        return context;
    }

    public PluginContext getPluginContext() {
        return pluginContext;
    }
}
