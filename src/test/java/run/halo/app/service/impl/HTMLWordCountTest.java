package run.halo.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import run.halo.app.utils.MarkdownUtils;

@Slf4j
public class HTMLWordCountTest {

    String markdownWithPicture = "图片字数测试\n" +
        "![image.png](http://127.0.0.1:8090/upload/2021/04/image-51142fdc369c48698dd75c24f6049738" +
        ".png)";

    String markdownWithTitle = "# 标题字数测试";

    String markdownWithFontType = "++~~***字体样式字数测试***~~++";

    String markdownWithCodeType = "`代码样式字数测试`";

    String markdownWithLink = "[链接字数测试](https://www.baidu.com)";

    String markdownWithTable = "|表格|字数|测试|\n" +
        "|-------|-------|-------|\n" +
        "|表格|字数|测试|";

    @Test
    void PictureTest() {
        assertEquals("图片字数测试".length(),
            BasePostServiceImpl.htmlFormatWordCount(MarkdownUtils.renderHtml(markdownWithPicture)));
    }

    @Test
    void TitleTest() {
        assertEquals("标题字数测试".length(),
            BasePostServiceImpl.htmlFormatWordCount(MarkdownUtils.renderHtml(markdownWithTitle)));
    }

    @Test
    void FontTypeTest() {
        assertEquals("字体样式字数测试".length(),
            BasePostServiceImpl.htmlFormatWordCount(MarkdownUtils.renderHtml(markdownWithFontType)));
    }

    @Test
    void CodeTypeTest() {
        assertEquals("代码样式字数测试".length(),
            BasePostServiceImpl.htmlFormatWordCount(MarkdownUtils.renderHtml(markdownWithCodeType)));
    }

    @Test
    void LinkTest() {
        assertEquals("链接字数测试".length(),
            BasePostServiceImpl.htmlFormatWordCount(MarkdownUtils.renderHtml(markdownWithLink)));
    }

    @Test
    void TableTest() {
        assertEquals("表格字数测试".length() * 2,
            BasePostServiceImpl.htmlFormatWordCount(MarkdownUtils.renderHtml(markdownWithTable)));
    }
}
