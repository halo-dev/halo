package run.halo.app.repository;

import run.halo.app.model.entity.Page;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * Page repository test.
 *
 * @author johnniang
 * @date 3/22/19
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class PageRepositoryTest {

    @Autowired
    private PageRepository pageRepository;

    @Test
    public void listAllTest() {
        List<Page> allPages = pageRepository.findAll();
        System.out.println(allPages);
    }
}
