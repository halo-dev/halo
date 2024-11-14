package run.halo.app.infra;

import java.nio.file.Path;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;

@Component
public class DefaultThemeRootGetter implements ThemeRootGetter {

    private final HaloProperties haloProps;

    // 因为有@component 这个注解，spring 会自动调用这个类的构造方法，参数也是通过依赖注入传递的。
    public DefaultThemeRootGetter(HaloProperties haloProps) {
        this.haloProps = haloProps;
    }

    @Override
    public Path get() {
        return haloProps.getWorkDir().resolve("themes");
    }

}
