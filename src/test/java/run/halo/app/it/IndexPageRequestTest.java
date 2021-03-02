package run.halo.app.it;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import run.halo.app.model.entity.Category;
import run.halo.app.service.CategoryService;

/**
 * Index page request test.
 *
 * @author johnniang
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
class IndexPageRequestTest {

    @Autowired
    RestTemplate restTemplate;

    @LocalServerPort
    int port;

    @Autowired
    CategoryService categoryService;

    @Test
    void contextLoad() {
    }

    @Test
    void indexPage() {
        Category category = new Category();
        category.setName("a category");
        category.setSlug("a slug");
        categoryService.create(category);
        String response = restTemplate.getForObject("http://localhost:" + port + "/", String.class);
        log.info("{}", response);
    }
}
