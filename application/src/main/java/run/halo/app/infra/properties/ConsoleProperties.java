package run.halo.app.infra.properties;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class ConsoleProperties {

    private String location = "classpath:/console/";

    @Valid
    private ProxyProperties proxy = new ProxyProperties();

}
