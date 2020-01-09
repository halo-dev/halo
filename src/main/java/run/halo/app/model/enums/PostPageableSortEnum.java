package run.halo.app.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PostPageableSortEnum class
 *
 * @author zhouchunjie
 * @date 2020/1/5
 */
@Getter
@AllArgsConstructor
public enum PostPageableSortEnum implements ValueEnum<String> {

    CREATE_TIME("create_time"),
    EDIT_TIME( "edit_time"),
    VISITS( "visits");

    private final String value;

    @Override
    public String getValue() {
        return value;
    }
}
