package run.halo.app.extension.index.query;

/**
 * Index condition interface for index-based queries.
 *
 * @author johnniang
 * @since 2.22.0
 */
public interface IndexCondition extends Condition {

    /**
     * Get the index name.
     *
     * @return the index name
     */
    String indexName();

}
