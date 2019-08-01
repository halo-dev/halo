package run.halo.app.repository;

import run.halo.app.model.entity.Sheet;
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
public class SheetRepositoryTest {

    @Autowired
    private SheetRepository sheetRepository;

    @Test
    public void listAllTest() {
        List<Sheet> allSheets = sheetRepository.findAll();
        System.out.println(allSheets);
    }
}
