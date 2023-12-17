package run.halo.app.actuator;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import run.halo.app.Application;

@WebEndpoint(id = "restart")
@Component
@Slf4j
public class RestartEndpoint implements ApplicationListener<ApplicationStartedEvent> {

    private SpringApplication application;

    private String[] args;

    private ConfigurableApplicationContext context;

    @WriteOperation
    public Object restart() {
        var threadGroup = new ThreadGroup("RestartGroup");
        var thread = new Thread(threadGroup, this::doRestart, "restartMain");
        thread.setDaemon(false);
        thread.setContextClassLoader(Application.class.getClassLoader());
        thread.start();
        return Collections.singletonMap("message", "Restarting");
    }

    private synchronized void doRestart() {
        log.info("Restarting...");
        if (this.context != null) {
            try {
                closeRecursively(this.context);
                var shutdownHandlers = SpringApplication.getShutdownHandlers();
                if (shutdownHandlers instanceof Runnable runnable) {
                    // clear closedContext in org.springframework.boot.SpringApplicationShutdownHook
                    runnable.run();
                }
                this.context = this.application.run(args);
                log.info("Restarted");
            } catch (Throwable t) {
                log.error("Failed to restart.", t);
            }
        }
    }

    private static void closeRecursively(ApplicationContext ctx) {
        while (ctx != null) {
            if (ctx instanceof Closeable closeable) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    log.error("Cannot close context: {}", ctx.getId(), e);
                }
            }
            ctx = ctx.getParent();
        }
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (this.context == null) {
            this.application = event.getSpringApplication();
            this.args = event.getArgs();
            this.context = event.getApplicationContext();
        }
    }
}
