package run.halo.app.handler.staticdeploy;

import org.springframework.lang.Nullable;
import run.halo.app.model.enums.StaticDeployType;

/**
 * Static deploy handler interface class.
 *
 * @author ryanwang
 * @date 2019-12-26
 */
public interface StaticDeployHandler {

    /**
     * do deploy.
     */
    void deploy();

    /**
     * Checks if the given type is supported.
     *
     * @param type deploy type
     * @return true if supported; false or else
     */
    boolean supportType(@Nullable StaticDeployType type);
}
