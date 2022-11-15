package run.halo.app.infra.properties;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class ThemeProperties {

    @Valid
    private final Initializer initializer = new Initializer();

    @Data
    public static class Initializer {

        private boolean disabled = false;

        private String location = "classpath:themes/theme-earth.zip";

    }

}
