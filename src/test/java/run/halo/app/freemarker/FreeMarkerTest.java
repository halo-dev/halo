package run.halo.app.freemarker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;


/**
 * @author LIlGG
 * @date 2021/3/5
 */
@SpringBootTest
@ActiveProfiles("test")
class FreeMarkerTest {
    @Autowired
    FreeMarkerConfigurer freeMarkerConfigurer;

    @Test
    public void testBlockRender() throws IOException, TemplateException {
        Configuration cfg = freeMarkerConfigurer.getConfiguration();
        cfg.setDefaultEncoding("UTF-8");

        StringTemplateLoader templateLoader = new StringTemplateLoader();
        templateLoader.putTemplate("common/macro/common_macro.ftl", "");
        templateLoader.putTemplate("common/macro/global_macro.ftl", "");
        templateLoader.putTemplate("index.ftl",
            "<@layout.extends name=\"layout/base.ftl\">\n"
                + "    <@layout.put block=\"title\" type=\"replace\">\n"
                + "        <title>自定义标题 - 替换模板内容</title>\n"
                + "    </@layout.put>\n"
                + "    <@layout.put block=\"header\">\n"
                + "        <h2>第二级页头 - 默认放置在模板内容之后</h2>\n"
                + "    </@layout.put>\n"
                + "    <@layout.put block=\"class\">sheet test-sheet</@layout.put>\n"
                + "    <@layout.put block=\"contents\">\n"
                + "        <p>这是自定义页面内容</p>\n"
                + "    </@layout.put>\n"
                + "    <@layout.put block=\"footer\" type=\"prepend\">\n"
                + "        <hr/>\n"
                + "        <div class=\"footer\">页脚内容 - 放置在模板内容之前</div>\n"
                + "    </@layout.put>\n"
                + "</@layout.extends>");
        templateLoader.putTemplate("layout/base.ftl",
            "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "    <@layout.block name=\"title\">\n"
                + "        <title>标题</title>\n"
                + "    </@layout.block>\n"
                + "</head>\n"
                + "<body>\n"
                + "<@layout.block name=\"header\">\n"
                + "    <h1>页头</h1>\n"
                + "</@layout.block>\n"
                + "<div class=\"content <@layout.block name='class'></@layout.block>\">\n"
                + "    <@layout.block name=\"content\">\n"
                + "    </@layout.block>\n"
                + "</div>\n"
                + "<@layout.block name=\"footer\">\n"
                + "    <div>页脚</div>\n"
                + "</@layout.block>\n"
                + "</body>\n"
                + "</html>");

        cfg.setTemplateLoader(templateLoader);

        freeMarkerConfigurer.setConfiguration(cfg);

        Template template = cfg.getTemplate("index.ftl");
        StringWriter out = new StringWriter();

        template.process(null, out);

        assertEquals(
            "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "            <title>自定义标题 - 替换模板内容</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "    <h1>页头</h1>\n"
                + "        <h2>第二级页头 - 默认放置在模板内容之后</h2>\n"
                + "<div class=\"content sheet test-sheet\">\n"
                + "</div>\n"
                + "        <hr/>\n"
                + "        <div class=\"footer\">页脚内容 - 放置在模板内容之前</div>\n"
                + "    <div>页脚</div>\n"
                + "</body>\n"
                + "</html>",
            out.toString());
    }
}
