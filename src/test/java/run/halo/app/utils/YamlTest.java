package run.halo.app.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;

import java.io.IOException;

/**
 * Yaml test.
 *
 * @author johnniang
 * @date 4/8/19
 */
public class YamlTest {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void readYamlTest() throws IOException {

        String yaml = "style:\n" +
                "  name: Style settings\n" +
                "  items:\n" +
                "    post_title_lower:\n" +
                "      name: post_title_lower\n" +
                "      description: Post title lower\n" +
                "      type: radio\n" +
                "      defaultValue: true\n" +
                "      options:\n" +
                "        - value: true\n" +
                "          label: Enabled\n" +
                "        - value: false\n" +
                "          label: Disabled\n" +
                "    custom_style:\n" +
                "      name: custom_style\n" +
                "      description: Custom style\n" +
                "      type: textarea\n";

        Object config = yamlMapper.readValue(yaml, Object.class);

        System.out.println(jsonMapper.writeValueAsString(config));
    }
}
