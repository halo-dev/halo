package run.halo.app.infra.properties;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
public class ConsoleProperties {

    private String location = "classpath:/console/";

    @Valid
    @NestedConfigurationProperty
    private ProxyProperties proxy = new ProxyProperties();

}
