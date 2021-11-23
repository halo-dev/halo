package run.halo.app.it;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import run.halo.app.model.params.InstallParam;

/**
 * Base api test.
 *
 * @author johnniang
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BaseApiTest {

    @Autowired
    TestRestTemplate restTemplate;

    @LocalServerPort
    int port;

    String blogUrl;

    @BeforeEach
    void baseSetUp() {
        blogUrl = "http://localhost:" + port;
    }

    void installBlog() {

        InstallParam install = new InstallParam();
        install.setUsername("test");
        install.setNickname("test");
        install.setEmail("test@test.com");
        install.setPassword("opentest");
        install.setUrl("http://localhost:" + port);
        install.setTitle("Test's Blog");

        restTemplate.postForObject(blogUrl + "/api/admin/installations", install,
            String.class);
    }

}
