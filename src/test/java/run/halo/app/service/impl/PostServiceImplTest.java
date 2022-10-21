package run.halo.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.vo.PostDetailVO;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@Disabled("Due to spring boot context needed")
class PostServiceImplTest {

    String standardMdContent = "---\n"
        + "title: springfox-swagger2配置成功但无法访问/swagger-ui.html\n"
        + "categories: \n"
        + "  - [后端,JAVA]\n"
        + "  - [Spring]\n"
        + "tags:\n"
        + "  - spring boot\n"
        + "  - swagger\n"
        + "  - solution\n"
        + "date: 2018-11-23 16:11:28\n"
        + "---\n"
        + "| 书名     | 作者       |\n"
        + "| -------- | ---------- |\n"
        + "| 《剑来》 | 烽火戏诸侯 |\n"
        + "# Pre\n"
        + "\n"
        + "在前后端分离项目中，通常需要用到 API 文档，springfox 开发的 **[SpringFox](https://github"
        + ".com/springfox/springfox)** 可以实现自动化 json API 文档。";

    String nonStandardMdContent = "---\n"
        + "title: Basic concepts of JPA\n"
        + "categories: \n"
        + "- [后端,JAVA]\n"
        + "- [Spring]\n"
        + "date: 2018-08-03 11:57:00\n"
        + "tags: ['spring', 'jpa', 'database', 'concept']\n"
        + "---\n"
        + "\n"
        + "以下将讲解关系型数据的关系描述。仅仅是作为总结。";


    String noFontMatterTable = "# 书单\n"
        + "| 书名     | 作者       |\n"
        + "| :-------- | ---------- |\n"
        + "| 《剑来》 | 烽火戏诸侯 |\n"
        + "## 剑来\n"
        + "\n"
        + "大千世界，无奇不有。天道崩塌，我陈平安，唯有一剑，可搬山，断江，倒海，降妖，镇魔，敕神，摘星，摧城，开天。";

    @Autowired
    PostServiceImpl postService;

    @Test
    void getContent() {
        String exportMarkdown = postService.exportMarkdown(1);
        log.debug(exportMarkdown);
    }

    @Test
    @Transactional
    void markdownImportTest() {
        PostDetailVO standardPost = postService.importMarkdown(standardMdContent, "standard");
        Map<String, CategoryDTO> standardCategoryMap = standardPost.getCategories().stream()
            .collect(Collectors.toMap(CategoryDTO::getName, post -> post));
        assertTrue(standardCategoryMap.containsKey("后端"));
        assertTrue(standardCategoryMap.containsKey("JAVA"));
        assertTrue(standardCategoryMap.containsKey("Spring"));
        assertTrue(standardCategoryMap.get("后端").getId()
            .equals(standardCategoryMap.get("JAVA").getParentId()));
        assertEquals(standardPost.getTags().size(), 3);
        assertTrue(standardPost.getContent().contains("书名"));
        assertFalse(standardPost.getContent().contains("16:11:28"));

        PostDetailVO nonStandardPost =
            postService.importMarkdown(nonStandardMdContent, "nonStandard");
        Map<String, CategoryDTO> nonStandardCategoryMap = nonStandardPost.getCategories().stream()
            .collect(Collectors.toMap(CategoryDTO::getName, post -> post));
        assertTrue(nonStandardCategoryMap.containsKey("后端"));
        assertTrue(nonStandardCategoryMap.containsKey("JAVA"));
        assertTrue(nonStandardCategoryMap.containsKey("Spring"));
        assertTrue(nonStandardCategoryMap.get("后端").getId()
            .equals(nonStandardCategoryMap.get("JAVA").getParentId()));
        assertEquals(nonStandardPost.getTags().size(), 4);

        PostDetailVO noFontMatterTablePost =
            postService.importMarkdown(noFontMatterTable, "noFontMatterTable");
        assertTrue(noFontMatterTablePost.getContent().contains("书单"));

    }
}