package run.halo.app.model.enums;

/**
 * Log type.
 *
 * @author johnniang
 */
public enum LogType implements ValueEnum<Integer> {

    POST_PUBLISHED(0),
    POST_EDITED(1),
    POST_DELETED(5),
    LOGGED_IN(2),
    LOGGED_OUT(3),
    LOGIN_FAILED(4),
    ;

    private final Integer value;

    LogType(Integer value) {
        this.value = value;
    }


    @Override
    public Integer getValue() {
        return value;
    }
}
