package cc.ryanc.halo.model.entity.enums;

/**
 * Post type.
 *
 * @author johnniang
 */
public enum PostType implements ValueEnum<Integer> {

    POST(0),
    PAGE(1),
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
