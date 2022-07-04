package run.halo.app.extension.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ControllerManager implements ApplicationListener<ApplicationReadyEvent>,
    DisposableBean {

    private final ApplicationContext applicationContext;

    public ControllerManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        applicationContext.getBeansOfType(Controller.class).values().forEach(Controller::start);
    }

    @Override
    public void destroy() {
        var controllers =
            applicationContext.getBeansOfType(Controller.class).values();
        log.info("Shutting down {} controllers...", controllers.size());
        controllers.forEach(
            controller -> {
                try {
                    controller.dispose();
                } catch (Throwable t) {
                    log.error("Failed to dispose controller {}", controller.getName(), t);
                }
            });
        log.info("Shutdown {} controllers.", controllers.size());
    }

}
