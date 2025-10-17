package run.halo.app.extension.index;

import java.io.Closeable;
import java.util.Set;
import run.halo.app.extension.Extension;

public interface Index<E extends Extension, K extends Comparable<K>> extends Closeable {

    String getName();

    Class<K> getKeyType();

    default boolean isUnique() {
        return false;
    }

    TransactionalOperation prepareInsert(E extension);

    TransactionalOperation prepareUpdate(E newExtension);

    TransactionalOperation prepareDelete(String primaryKey);

    Set<K> getKeys(String primaryKey);

}
