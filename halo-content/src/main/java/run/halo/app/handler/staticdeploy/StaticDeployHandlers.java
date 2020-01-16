package run.halo.app.handler.staticdeploy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.FileOperationException;
import run.halo.app.model.enums.StaticDeployType;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Static deploy handlers.
 *
 * @author ryanwang
 * @date 2019-12-26
 */
@Slf4j
@Component
public class StaticDeployHandlers {

    private final Collection<StaticDeployHandler> staticDeployHandlers = new LinkedList<>();

    public StaticDeployHandlers(ApplicationContext applicationContext) {
        // Add all static deploy handler
        addStaticDeployHandlers(applicationContext.getBeansOfType(StaticDeployHandler.class).values());
    }


    /**
     * do deploy.
     *
     * @param staticDeployType static deploy type
     */
    public void deploy(@NonNull StaticDeployType staticDeployType) {
        Assert.notNull(staticDeployType, "Static deploy type must not be null");

        for (StaticDeployHandler staticDeployHandler : staticDeployHandlers) {
            if (staticDeployHandler.supportType(staticDeployType)) {
                staticDeployHandler.deploy();
                return;
            }
        }

        throw new FileOperationException("No available static deploy handler to deploy static pages").setErrorData(staticDeployType);
    }

    /**
     * Adds static deploy handlers.
     *
     * @param staticDeployHandlers static deploy handler collection
     * @return current file handlers
     */
    @NonNull
    public StaticDeployHandlers addStaticDeployHandlers(@Nullable Collection<StaticDeployHandler> staticDeployHandlers) {
        if (!CollectionUtils.isEmpty(staticDeployHandlers)) {
            this.staticDeployHandlers.addAll(staticDeployHandlers);
        }
        return this;
    }
}
