package cc.ryanc.halo;

import cc.ryanc.halo.repository.base.BaseRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * <pre>
 *     Halo run!
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/11/14
 */
@Slf4j
@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "cc.ryanc.halo.repository", repositoryBaseClass = BaseRepositoryImpl.class)
public class Application {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class, args);
        String serverPort = context.getEnvironment().getProperty("server.port");
        log.info("Halo started at http://localhost:" + serverPort);
    }
}
