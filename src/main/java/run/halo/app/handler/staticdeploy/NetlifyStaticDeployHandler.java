package run.halo.app.handler.staticdeploy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import run.halo.app.model.enums.StaticDeployType;
import run.halo.app.service.OptionService;

/**
 * Netlify deploy handler.
 *
 * @author ryanwang
 * @date 2019-12-26
 */
@Slf4j
@Component
public class NetlifyStaticDeployHandler implements StaticDeployHandler {

    private final OptionService optionService;

    public NetlifyStaticDeployHandler(OptionService optionService) {
        this.optionService = optionService;
    }

    @Override
    public void deploy() {

    }

    @Override
    public boolean supportType(StaticDeployType type) {
        return StaticDeployType.NETLIFY.equals(type);
    }
}
