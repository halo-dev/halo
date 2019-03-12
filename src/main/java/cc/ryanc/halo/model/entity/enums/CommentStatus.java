package cc.ryanc.halo.model.entity.enums;

/**
 * Comment status.
 *
 * @author johnniang
 */
public enum CommentStatus implements ValueEnum<Integer> {

    PUBLISHED(0),
    AUDITING(1),
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
