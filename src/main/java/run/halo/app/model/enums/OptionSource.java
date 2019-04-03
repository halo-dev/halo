package run.halo.app.model.enums;

/**
 * Option source.
 *
 * @author johnniang
 * @date 4/1/19
 */
public enum OptionSource implements ValueEnum<Integer> {

    SYSTEM(0),
    THEME(1);

    private final int value;

    OptionSource(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return null;
    }}
