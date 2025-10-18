package run.halo.app.extension.index;

import run.halo.app.extension.Extension;

public interface IndexSpecBuilder<E extends Extension, K extends Comparable<K>> {

    ValueIndexSpec<E, K> build();

}
