package run.halo.app.extension.index;

/**
 * Represents a transactional operation with prepare, commit, and rollback methods.
 *
 * @author johnniang
 * @since 2.20.0
 */
public interface TransactionalOperation {

    /**
     * Prepares the operation for execution. Implementation should perform necessary checks here and
     * save any state needed for rollback.
     */
    void prepare();

    /**
     * Commits the operation.
     */
    void commit();

    /**
     * Rolls back the operation.
     */
    void rollback();

}
