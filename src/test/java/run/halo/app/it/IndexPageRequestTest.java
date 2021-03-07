package run.halo.app.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

/**
 * Index page request test.
 *
 * @author johnniang
 */
@Slf4j
class IndexPageRequestTest extends BaseApiTest {

    @Test
    void indexPage() throws IOException {
        installBlog();
        // validate atom.xml link
        Document document = Jsoup.connect(blogUrl).get();
        Element atomLink = document.head().getElementsByAttributeValue("title", "atom 1.0").get(0);
        assertEquals(blogUrl + "/atom.xml", atomLink.attr("href"));

        // validate title link
        Element titleLink = document.body().selectFirst(".logo-title > .title > h3 > a");
        assertEquals(blogUrl, titleLink.attr("href"));
        assertEquals("Test's Blog", titleLink.text());

        // validate post link
        Element postTitleLink =
            document.body().selectFirst(".content > .post > .post-title > h3 > a");
        assertEquals(blogUrl + "/archives/hello-halo", postTitleLink.attr("href"));
        assertEquals("Hello Halo", postTitleLink.text());
    }
}
