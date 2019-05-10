package run.halo.app;

import org.springframework.scheduling.annotation.EnableAsync;
import run.halo.app.repository.base.BaseRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * <pre>
 *     Halo run!
 * </pre>
 *
 * @author ryanwang
 * @date : 2017/11/14
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
@EnableJpaRepositories(basePackages = "run.halo.app.repository", repositoryBaseClass = BaseRepositoryImpl.class)
public class Application {

    public static void main(String[] args) {
        // Customize the spring config location
        System.setProperty("spring.config.additional-location", "file:${user.home}/.halo/,file:${user.home}/halo-dev/");

        // Run application
        SpringApplication.run(Application.class, args);
    }
}
