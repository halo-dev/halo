package run.halo.app;

import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import run.halo.app.utils.VmUtils;

/**
 * Halo main class.
 *
 * @author ryanwang
 * @date 2017-11-14
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // Customize the spring config location
        System.setProperty("spring.config.additional-location",
            "optional:file:${user.home}/.halo/,optional:file:${user.home}/halo-dev/");

        // Run application
        SpringApplication.run(Application.class, args);
    }

}
