package run.halo.app.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import org.junit.Test;
import org.springframework.lang.Nullable;
import run.halo.app.model.enums.DataType;
import run.halo.app.model.enums.InputType;

import java.io.IOException;
import java.util.*;

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

        LinkedHashMap config = yamlMapper.readValue(yaml, LinkedHashMap.class);

        System.out.println(config.getClass());

        System.out.println(jsonMapper.writeValueAsString(config));
    }

    @Test
    @SuppressWarnings("unchecked")
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

        Object config = yamlMapper.readValue(yaml, Object.class);

        List<Tab> tabs = handleTabs(config);

        System.out.println(config.getClass());

        System.out.println(tabs);

        System.out.println(jsonMapper.writeValueAsString(config));
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


        Object config = yamlMapper.readValue(yaml, Object.class);

        List<Tab> tabs = handleTabs(config);

        System.out.println(config.getClass());

        System.out.print(tabs);
    }

    @SuppressWarnings("unchecked")
    private List<Tab> handleTabs(@Nullable Object config) {
        List<Tab> tabs = new LinkedList<>();

        if (config instanceof List) {
            List configList = (List) config;

            // Resolve tab

            configList.forEach(tabYaml -> {
                // tabYaml should be map
                if (!(tabYaml instanceof Map)) {
                    return;
                }

                Map tabMap = ((Map) tabYaml);

                Tab tab = new Tab();

                tab.setName(tabMap.get("name").toString());
                tab.setLabel(tabMap.get("label").toString());

                // Handle items
                tab.setItems(handleItems(tabMap.get("items")));

                tabs.add(tab);
            });

        } else if (config instanceof Map) {
            Map configMap = (Map) config;
            configMap.forEach((key, value) -> {
                // key: tab name
                // value: tab property, should be a map
                if (!(value instanceof Map)) {
                    return;
                }

                Map tabMap = (Map) value;

                Tab tab = new Tab();

                tab.setName(key.toString());
                tab.setLabel(tabMap.get("label").toString());

                // Handle items
                tab.setItems(handleItems(tabMap.get("items")));

                tabs.add(tab);
            });
        }

        return tabs;
    }

    @SuppressWarnings("unchecked")
    private List<Item> handleItems(@Nullable Object items) {

        if (items == null) {
            return Collections.emptyList();
        }

        List<Item> result = new LinkedList<>();

        if (items instanceof List) {
            ((List) items).forEach(itemYaml -> {
                if (!(itemYaml instanceof Map)) {
                    return;
                }

                // Should be Map
                Map itemMap = (Map) itemYaml;

                // Build item
                Item item = new Item();

                item.setName(itemMap.get("name").toString());
                item.setLabel(itemMap.getOrDefault("label", item.getName()).toString());
                item.setDataType(DataType.typeOf(itemMap.get("data_type")));
                item.setType(InputType.typeOf(itemMap.get("type")));
                item.setDefaultValue(itemMap.get("default"));

                // Handle options
                item.setOptions(handleOptions(itemMap.get("options")));

                // Add item
                result.add(item);
            });
        } else if (items instanceof Map) {
            Map itemsMap = (Map) items;

            itemsMap.forEach((key, value) -> {
                if (!(value instanceof Map)) {
                    return;
                }

                Map itemMap = (Map) value;

                // key: item name
                Item item = new Item();
                item.setName(key.toString());
                item.setLabel(itemMap.getOrDefault("label", item.getName()).toString());
                item.setDataType(DataType.typeOf(itemMap.get("data_type")));
                item.setType(InputType.typeOf(itemMap.get("type")));
                item.setDefaultValue(itemMap.get("default"));

                // Handle options
                item.setOptions(handleOptions(itemMap.get("options")));

                // Add item
                result.add(item);
            });
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Option> handleOptions(@Nullable Object options) {

        if (options == null) {
            return Collections.emptyList();
        }

        List<Option> result = new LinkedList<>();

        if (options instanceof List) {
            ((List) options).forEach(optionYaml -> {
                // optionYaml should be Map
                if (!(optionYaml instanceof Map)) {
                    return;
                }

                Map optionMap = (Map) optionYaml;

                // Build option
                Option option = new Option();
                // TODO Convert the value type
                option.setValue(optionMap.get("value"));
                option.setLabel(optionMap.get("label").toString());

                result.add(option);
            });
        } else if (options instanceof Map) {
            Map optionsMap = (Map) options;
            optionsMap.forEach((key, value) -> {
                // key: option value
                // value: Map that contains option label

                if (!(value instanceof Map)) {
                    return;
                }

                Map optionMap = (Map) value;


                Option option = new Option();
                // TODO Convert the value type
                option.setValue(key);
                option.setLabel(optionMap.get("label").toString());

                result.add(option);
            });
        }

        return result;
    }

    @Data
    private class Tab {
        private String name;
        private String label;
        private List<Item> items;
    }

    @Data
    private class Item {
        private String name;
        private String label;
        private InputType type;
        private DataType dataType;
        private Object defaultValue;

        private List<Option> options;
    }

    @Data
    private class Option {
        private String label;
        private Object value;
    }
}
