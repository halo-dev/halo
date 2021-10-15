package run.halo.app.model.enums;

/**
 * Static deploy type.
 *
 * @author ryanwang
 * @date 2019-12-26
 */
public enum StaticDeployType implements ValueEnum<Integer> {

    /**
     * Deploy static pages in remote git repository, such as github pages,gitee pages,coding
     * pages.etc.
     */
    GIT(0),

    /**
     * Deploy static pages in netlify.
     */
    NETLIFY(1);

    private final Integer value;

    StaticDeployType(Integer value) {
        this.value = value;
    }

    /**
     * Get enum value.
     *
     * @return enum value
     */
    @Override
    public Integer getValue() {
        return value;
    }
}
