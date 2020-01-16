package run.halo.app.model.enums;

/**
 * Journal type.
 *
 * @author ryanwnag
 */
public enum JournalType implements ValueEnum<Integer> {

    /**
     * Public type.
     */
    PUBLIC(1),

    /**
     * Intimate type.
     */
    INTIMATE(0);

    private final int value;

    JournalType(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
