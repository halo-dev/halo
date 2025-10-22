package run.halo.app.extension.index.query;

/**
 * Label condition interface for label-based queries.
 *
 * @author johnniang
 * @since 2.22.0
 */
public interface LabelCondition extends Condition {

    String INDEX_NAME = "metadata.labels";

    /**
     * Get the label key.
     *
     * @return the label key
     */
    String labelKey();

    @Override
    LabelCondition not();

}
