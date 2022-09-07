package run.halo.app.infra;

import java.net.URI;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Default implementation for getting external url from halo properties.
 *
 * @author johnniang
 */
@Component
public class HaloPropertiesExternalUrlSupplier implements ExternalUrlSupplier {

    private final HaloProperties haloProperties;

    public HaloPropertiesExternalUrlSupplier(HaloProperties haloProperties) {
        this.haloProperties = haloProperties;
    }

    @Override
    public URI get() {
        return haloProperties.getExternalUrl();
    }
}
