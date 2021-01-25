package run.halo.app.handler.theme.config.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import run.halo.app.handler.theme.config.ThemeConfigResolver;
import run.halo.app.handler.theme.config.support.Group;
import run.halo.app.handler.theme.config.support.Item;
import run.halo.app.handler.theme.config.support.Option;
import run.halo.app.model.enums.DataType;
import run.halo.app.model.enums.InputType;

/**
 * Theme configuration resolver.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-10
 */
@Slf4j
@Service
public class YamlThemeConfigResolverImpl implements ThemeConfigResolver {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Override
    public List<Group> resolve(String content) throws IOException {
        return handleTabs(yamlMapper.readValue(content, Object.class));
    }

    @SuppressWarnings("unchecked")
    private List<Group> handleTabs(@Nullable Object config) {
        List<Group> groups = new LinkedList<>();

        if (config instanceof List) {
            List configList = (List) config;

            // Resolve tab
            configList.forEach(tabYaml -> {
                // tabYaml should be map
                if (!(tabYaml instanceof Map)) {
                    return;
                }

                Map tabMap = (Map) tabYaml;

                Group group = new Group();

                group.setName(tabMap.get("name").toString());
                group.setLabel(tabMap.get("label").toString());

                // Handle items
                group.setItems(handleItems(tabMap.get("items")));

                groups.add(group);
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

                Group group = new Group();

                group.setName(key.toString());
                group.setLabel(tabMap.get("label").toString());

                // Handle items
                group.setItems(handleItems(tabMap.get("items")));

                groups.add(group);
            });
        }

        return groups;
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
                Object dataType = itemMap.getOrDefault("data-type", itemMap.get("dataType"));
                item.setType(InputType.typeOf(itemMap.get("type")));
                item.setDataType(item.getType().equals(InputType.SWITCH) ? DataType.BOOL :
                    DataType.typeOf(dataType));
                item.setDefaultValue(itemMap.get("default"));
                item.setPlaceholder(itemMap.getOrDefault("placeholder", "").toString());
                item.setDescription(itemMap.getOrDefault("description", "").toString());

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
                Object dataType = itemMap.getOrDefault("data-type", itemMap.get("dataType"));
                item.setType(InputType.typeOf(itemMap.get("type")));
                item.setDataType(item.getType().equals(InputType.SWITCH) ? DataType.BOOL :
                    DataType.typeOf(dataType));
                item.setDefaultValue(itemMap.get("default"));
                item.setPlaceholder(itemMap.getOrDefault("placeholder", "").toString());
                item.setDescription(itemMap.getOrDefault("description", "").toString());

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
                option.setValue(key);
                option.setLabel(optionMap.get("label").toString());

                result.add(option);
            });
        }

        return result;
    }
}
