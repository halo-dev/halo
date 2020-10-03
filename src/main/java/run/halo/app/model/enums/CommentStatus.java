package run.halo.app.model.enums;

/**
 * Comment status.
 *
 * @author johnniang
 */
public enum CommentStatus implements ValueEnum<Integer> {

    /**
     * 已发布
     */
    PUBLISHED(0),

    /**
     * Auditing status.
     */
    AUDITING(1),

    /**
     * 回收站
     */
    RECYCLE(2);

    private final Integer value;

    CommentStatus(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
