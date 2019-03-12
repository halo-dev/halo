package cc.ryanc.halo.model.entity.enums;

/**
 * Post create from type.
 *
 * @author johnniang
 */
public enum PostCreateFrom implements ValueEnum<Integer> {

    ADMIN(0),
    WECHAT(1);

    private final Integer value;

    PostCreateFrom(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
