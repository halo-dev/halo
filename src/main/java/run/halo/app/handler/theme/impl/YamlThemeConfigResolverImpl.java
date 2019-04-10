package run.halo.app.handler.theme.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import run.halo.app.handler.theme.Item;
import run.halo.app.handler.theme.Option;
import run.halo.app.handler.theme.Tab;
import run.halo.app.handler.theme.ThemeConfigResolver;
import run.halo.app.model.enums.DataType;
import run.halo.app.model.enums.InputType;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Theme configuration resolver.
 *
 * @author johnniang
 * @date 4/10/19
 */
@Component
public class YamlThemeConfigResolverImpl implements ThemeConfigResolver {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Override
    public List<Tab> resolve(String content) throws IOException {
        return handleTabs(yamlMapper.readValue(content, Object.class));
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
}
