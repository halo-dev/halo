package run.halo.app.handler.theme.config.support;

import lombok.Data;
import run.halo.app.model.enums.DataType;
import run.halo.app.model.enums.InputType;

import java.util.List;
import java.util.Objects;

/**
 * Theme configuration: item entity
 *
 * @author johnniang
 * @date 4/10/19
 */
@Data
public class Item {

    /**
     * Item name.
     */
    private String name;

    /**
     * Item label.
     */
    private String label;

    /**
     * Item input type, default is text.
     */
    private InputType type;

    /**
     * Item data type, default is string.
     */
    private DataType dataType;

    /**
     * Item default value.
     */
    private Object defaultValue;

    /**
     * Text item placeholder.
     */
    private String placeholder;

    /**
     * Item's options, default is empty list
     */
    private List<Option> options;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Item item = (Item) o;
        return name.equals(item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
