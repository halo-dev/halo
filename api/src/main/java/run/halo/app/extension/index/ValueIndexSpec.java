package run.halo.app.extension.index;

import run.halo.app.extension.Extension;

public interface ValueIndexSpec<E extends Extension, K extends Comparable<K>> {

    String getName();

    boolean isUnique();

    boolean isNullable();

    Class<K> getKeyType();

}
