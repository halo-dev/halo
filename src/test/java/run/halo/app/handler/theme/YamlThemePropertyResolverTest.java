package run.halo.app.handler.theme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import run.halo.app.handler.theme.config.support.ThemeProperty;

import java.io.IOException;

/**
 * @author johnniang
 * @date 4/11/19
 */
@Slf4j
public class YamlThemePropertyResolverTest {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Test
    public void directResolveTest() throws IOException {
        String yaml = "id: viosey_material\n" +
            "name: Material\n" +
            "author:\n" +
            "  name: Viosey\n" +
            "  website: https://viosey.com\n" +
            "description: Nature, Pure | 原质，纯粹\n" +
            "logo: https://avatars0.githubusercontent.com/u/8141232?s=460&v=4\n" +
            "website: https://github.com/viosey/hexo-theme-material\n" +
            "version: 1.0";

        ThemeProperty themeProperty = yamlMapper.readValue(yaml, ThemeProperty.class);

        log.debug("[{}]", themeProperty);
    }
}