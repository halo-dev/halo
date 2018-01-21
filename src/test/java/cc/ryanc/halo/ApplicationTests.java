package cc.ryanc.halo;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.repository.CategoryRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private CacheManager cacheManager;

	@Test
	public void contextLoads() {
		List<Category> list = categoryRepository.findAll();
		System.out.println("第一次查询："+list.get(0).getCateName());

		List<Category> list2 = categoryRepository.findAll();
		System.out.println("第二次查询："+list.get(0).getCateName());
	}

}
