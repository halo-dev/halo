package cc.ryanc.halo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author RYAN0UP
 * SpringBoot启动类
 */
@Slf4j
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		log.info("Halo started at http://localhost:8090");
	}
}
