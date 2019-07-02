package run.halo.app.service.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class PostServiceImplTest {

    String standardMdContent = "---\n" +
            "title: springfox-swagger2配置成功但无法访问/swagger-ui.html\n" +
            "tags:\n" +
            "  - spring boot\n" +
            "  - swagger\n" +
            "  - solution\n" +
            "date: 2018-11-23 16:11:28\n" +
            "---\n" +
            "\n" +
            "# Pre\n" +
            "\n" +
            "在前后端分离项目中，通常需要用到 API 文档，springfox 开发的 **[SpringFox](https://github.com/springfox/springfox)** 可以实现自动化 json API 文档。";

    String nonStandardMdContent = "---\n" +
            "title: Basic concepts of JPA\n" +
            "date: 2018-08-03 11:57:00\n" +
            "tags: ['spring', 'jpa', 'database', 'concept']\n" +
            "---\n" +
            "\n" +
            "以下将讲解关系型数据的关系描述。仅仅是作为总结。";

    @Autowired
    private PostServiceImpl postService;

    @Test
    @Ignore
    public void getContent() {
        String exportMarkdown = postService.exportMarkdown(18);
        System.out.println(exportMarkdown);
    }

    @Test
    public void markdownImportTest() {
        postService.importMarkdown(standardMdContent, "standard");
        postService.importMarkdown(nonStandardMdContent, "nonStandard");
    }
}