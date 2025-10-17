package run.halo.app.extension.index;

public interface TransactionalOperation {

    void prepare();

    void commit();

    void rollback();

}
