package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Disabled("Due to spring boot context needed")
class PostServiceImplTest {

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
    PostServiceImpl postService;

    @Test
    void getContent() {
        String exportMarkdown = postService.exportMarkdown(18);
        log.debug(exportMarkdown);
    }

    @Test
    @Transactional
    void markdownImportTest() {
        postService.importMarkdown(standardMdContent, "standard");
        postService.importMarkdown(nonStandardMdContent, "nonStandard");
    }
}