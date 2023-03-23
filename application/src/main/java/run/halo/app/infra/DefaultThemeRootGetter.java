package run.halo.app.infra;

import java.nio.file.Path;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;

@Component
public class DefaultThemeRootGetter implements ThemeRootGetter {

    private final HaloProperties haloProps;

    public DefaultThemeRootGetter(HaloProperties haloProps) {
        this.haloProps = haloProps;
    }

    @Override
    public Path get() {
        return haloProps.getWorkDir().resolve("themes");
    }

}
