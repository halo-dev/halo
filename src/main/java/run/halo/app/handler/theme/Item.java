package run.halo.app.handler.theme;

import lombok.Data;
import run.halo.app.model.enums.DataType;
import run.halo.app.model.enums.InputType;

import java.util.List;

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
     * Item's options, default is empty list
     */
    private List<Option> options;
}
