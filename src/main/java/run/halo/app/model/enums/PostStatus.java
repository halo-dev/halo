package run.halo.app.model.enums;

/**
 * Post status.
 *
 * @author johnniang
 */
public enum PostStatus implements ValueEnum<Integer> {

    /**
     * Published status.
     */
    PUBLISHED(0),

    /**
     * Draft status.
     */
    DRAFT(1),

    /**
     * Recycle status.
     */
    RECYCLE(2),

    /**
     * Intimate status
     */
    INTIMATE(3);

    private final int value;

    PostStatus(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
