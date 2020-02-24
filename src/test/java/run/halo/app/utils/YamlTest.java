package run.halo.app.utils;

import org.junit.Test;
import run.halo.app.handler.theme.config.impl.YamlThemeConfigResolverImpl;
import run.halo.app.handler.theme.config.support.Group;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Yaml test.
 *
 * @author johnniang
 * @date 4/8/19
 */
public class YamlTest {

    private final YamlThemeConfigResolverImpl resolver = new YamlThemeConfigResolverImpl();

    @Test
    public void readYamlTest() throws IOException {

        String yaml = "sns:\n" +
            "  label: 社交资料设置\n" +
            "  items:\n" +
            "    rss:\n" +
            "      name: rss\n" +
            "      label: RSS\n" +
            "      type: radio\n" +
            "      default: true\n" +
            "      options:\n" +
            "        - value: true\n" +
            "          label: 开启\n" +
            "        - value: false\n" +
            "          label: 关闭\n" +
            "    twitter:\n" +
            "      name: twitter\n" +
            "      label: Twitter\n" +
            "      type: text\n" +
            "    facebook:\n" +
            "      name: facebook\n" +
            "      label: Facebook\n" +
            "      type: text\n" +
            "style:\n" +
            "  label: 样式设置\n" +
            "  items:\n" +
            "    icon:\n" +
            "      name: icon\n" +
            "      label: 右上角图标\n" +
            "      type: text\n" +
            "    post_title_uppper:\n" +
            "      name: post_title_uppper\n" +
            "      label: 文章标题大写\n" +
            "      type: radio\n" +
            "      default: true\n" +
            "      options:\n" +
            "        - value: true\n" +
            "          label: 开启\n" +
            "        - value: false\n" +
            "          label: 关闭";

        List<Group> groups = resolver.resolve(yaml);

        assertThat(groups.size(), equalTo(2));
        assertThat(groups.get(0).getItems().size(), equalTo(3));
        assertThat(groups.get(1).getItems().size(), equalTo(2));
    }

    @Test
    public void readAnotherYamlTest() throws IOException {
        String yaml = "sns:\n" +
            "  label: 社交资料设置\n" +
            "  items:\n" +
            "    - name: rss\n" +
            "      label: RSS\n" +
            "      type: radio\n" +
            "      default: true\n" +
            "      options:\n" +
            "        - value: true\n" +
            "          label: 开启\n" +
            "        - value: false\n" +
            "          label: 关闭\n" +
            "    - name: twitter\n" +
            "      label: Twitter\n" +
            "      type: text\n" +
            "    - name: facebook\n" +
            "      label: Facebook\n" +
            "      type: text\n" +
            "    - name: instagram\n" +
            "      label: Instagram\n" +
            "      type: text\n" +
            "style:\n" +
            "  label: 样式设置\n" +
            "  items:\n" +
            "    - name: icon\n" +
            "      label: 右上角图标\n" +
            "      type: text\n" +
            "    - name: post_title_uppper\n" +
            "      label: 文章标题大写\n" +
            "      type: radio\n" +
            "      default: true\n" +
            "      options:\n" +
            "        - value: true\n" +
            "          label: 开启\n" +
            "        - value: false\n" +
            "          label: 关闭\n" +
            "    - name: blog_title_uppper\n" +
            "      label: 博客标题大写\n" +
            "      type: radio\n" +
            "      default: true\n" +
            "      options:\n" +
            "        - value: true\n" +
            "          label: 开启\n" +
            "        - value: false\n" +
            "          label: 关闭\n";


        List<Group> groups = resolver.resolve(yaml);

        assertThat(groups.size(), equalTo(2));
        assertThat(groups.get(0).getItems().size(), equalTo(4));
        assertThat(groups.get(1).getItems().size(), equalTo(3));
    }

    @Test
    public void convertYamlTest() throws IOException {
        String yaml = "- name: sns\n" +
            "  label: 社交资料设置\n" +
            "  items:\n" +
            "    - name: rss\n" +
            "      label: RSS\n" +
            "      type: radio\n" +
            "      default: true\n" +
            "      options:\n" +
            "        - value: true\n" +
            "          label: 开启\n" +
            "        - value: false\n" +
            "          label: 关闭\n" +
            "    - name: twitter\n" +
            "      label: Twitter\n" +
            "      type: text\n" +
            "    - name: facebook\n" +
            "      label: Facebook\n" +
            "      type: text\n" +
            "    - name: instagram\n" +
            "      label: Instagram\n" +
            "      type: text\n" +
            "- name: style\n" +
            "  label: 样式设置\n" +
            "  items:\n" +
            "    - name: icon\n" +
            "      label: 右上角图标\n" +
            "      type: text\n" +
            "    - name: post_title_uppper\n" +
            "      label: 文章标题大写\n" +
            "      type: radio\n" +
            "      default: true\n" +
            "      options:\n" +
            "        - value: true\n" +
            "          label: 开启\n" +
            "        - value: false\n" +
            "          label: 关闭\n" +
            "    - name: blog_title_uppper\n" +
            "      label: 博客标题大写\n" +
            "      type: radio\n" +
            "      default: true\n" +
            "      options:\n" +
            "        - value: true\n" +
            "          label: 开启\n" +
            "        - value: false\n" +
            "          label: 关闭";

        List<Group> groups = resolver.resolve(yaml);

        assertThat(groups.size(), equalTo(2));
        assertThat(groups.get(0).getItems().size(), equalTo(4));
        assertThat(groups.get(1).getItems().size(), equalTo(3));
        assertThat(groups.get(0).getItems().get(0).getOptions().size(), equalTo(2));
    }

}
