package run.halo.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.properties.JwtProperties;

/**
 * Halo main class.
 *
 * @author ryanwang
 * @author JohnNiang
 * @author guqing
 * @date 2017-11-14
 */
@SpringBootApplication
@EnableConfigurationProperties({HaloProperties.class, JwtProperties.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
