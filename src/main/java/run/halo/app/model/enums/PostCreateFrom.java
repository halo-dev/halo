package run.halo.app.model.enums;

/**
 * Post create from type.
 *
 * @author johnniang
 */
public enum PostCreateFrom implements ValueEnum<Integer> {

    /**
     * 发布来源：管理后台
     */
    ADMIN(0),

    /**
     * 发布来源：微信
     */
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
