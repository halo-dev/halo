package run.halo.app.extension.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

@Slf4j
public class ControllerManager implements ApplicationListener<ApplicationReadyEvent>,
    ApplicationContextAware, DisposableBean {

    private ApplicationContext applicationContext;

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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
