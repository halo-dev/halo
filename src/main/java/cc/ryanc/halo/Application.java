package cc.ryanc.halo;

import cc.ryanc.halo.config.properties.HaloProperties;
import cc.ryanc.halo.logging.Logger;
import cc.ryanc.halo.repository.base.BaseRepositoryImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * <pre>
 *     Halo run!
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/11/14
 */
@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
@EnableScheduling
@EnableJpaRepositories(basePackages = "cc.ryanc.halo.repository", repositoryBaseClass = BaseRepositoryImpl.class)
public class Application {

    private final static Logger LOG = Logger.getLogger(Application.class);

    public static void main(String[] args) {
        // Run application
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner runner(ApplicationContext context, HaloProperties haloProperties) {
        return args -> {
            // Get server port
            String serverPort = context.getEnvironment().getProperty("server.port");

            LOG.debug("Halo started at    {}:{}", "http://localhost", serverPort);

            if (!haloProperties.getDocDisabled()) {
                LOG.debug("Halo doc enable at {}:{}/{}", "http://localhost", serverPort, "swagger-ui.html");
            }
        };
    }
}
