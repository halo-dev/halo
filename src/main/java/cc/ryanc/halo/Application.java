package cc.ryanc.halo;

import cc.ryanc.halo.logging.Logger;
import cc.ryanc.halo.repository.base.BaseRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
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
@EnableJpaRepositories(basePackages = {"cc.ryanc.halo.repository"}, repositoryBaseClass = BaseRepositoryImpl.class)
public class Application {

    private final static Logger LOG = Logger.getLogger(Application.class);

    public static void main(String[] args) {
        // Customize the spring config location
        System.setProperty("spring.config.additional-location", "file:${user.home}/halo/,file:${user.home}/halo-dev/");

        // Run application
        SpringApplication.run(Application.class, args);
    }
}
