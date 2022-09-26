package run.halo.app;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.properties.JwtProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Halo main class.
 *
 * @author ryanwang
 * @author JohnNiang
 * @author guqing
 * @date 2017-11-14
 */
@SpringBootApplication(scanBasePackages = "run.halo.app", exclude =
    IntegrationAutoConfiguration.class)
@EnableConfigurationProperties({HaloProperties.class, JwtProperties.class})
public class Application {

    public static void main(String[] args) {
        System.setProperty("halo.work-dir",
            StringUtils.isNoneEmpty(System.getProperty("halo.work-dir")) ?
                URLEncoder.encode(System.getProperty("user.home"), StandardCharsets.UTF_8)
                    + "/halo-next"
                : URLEncoder.encode(System.getProperty("user.home"), StandardCharsets.UTF_8)
                    + System.getProperty("halo.work-dir"));
        SpringApplication.run(Application.class, args);
    }

}
