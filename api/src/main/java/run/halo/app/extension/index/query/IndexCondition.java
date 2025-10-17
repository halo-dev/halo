package run.halo.app.extension.index.query;

public interface IndexCondition extends Condition {

    /**
     * Get the index name.
     *
     * @return the index name
     */
    String indexName();

}
