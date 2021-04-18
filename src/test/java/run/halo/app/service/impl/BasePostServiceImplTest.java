package run.halo.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class BasePostServiceImplTest {

    String markdownWithPicture = "字数测试，测试这篇文章加上图片有没有问题\n" +
        "![image.png](http://127.0.0.1:8090/upload/2021/04/image-51142fdc369c48698dd75c24f6049738" +
        ".png)\n" +
        "![image.png](http://127.0.0.1:8090/upload/2021/04/image-f7e2b01b895c427e8dc6a44f4affc1b7" +
        ".png)";

    @Test
    @Transactional
    void wordCountTest() {
        assertEquals(20, BasePostServiceImpl.markdownWordCount(markdownWithPicture));
    }
}
