package run.halo.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import run.halo.app.repository.base.BaseRepositoryImpl;

/**
 * Halo main class.
 *
 * @author ryanwang
 * @date 2017-11-14
 */
@SpringBootApplication
@EnableAsync
@EnableJpaRepositories(basePackages = "run.halo.app.repository", repositoryBaseClass = BaseRepositoryImpl.class)
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        // Customize the spring config location
        System.setProperty("spring.config.additional-location", "file:${user.home}/.halo/,file:${user.home}/halo-dev/");

        // Run application
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        System.setProperty("spring.config.additional-location", "file:${user.home}/.halo/,file:${user.home}/halo-dev/");
        return application.sources(Application.class);
    }
}
