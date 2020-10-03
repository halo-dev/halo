package run.halo.app.model.enums;

/**
 * Post type.
 *
 * @author johnniang
 */
@Deprecated
public enum PostType implements ValueEnum<Integer> {

    /**
     * 普通文章
     */
    POST(0),

    /**
     * 自定义页面
     */
    PAGE(1),

    /**
     * 日志
     */
    JOURNAL(2);

    private final Integer value;

    PostType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
