package run.halo.app.extension;

import run.halo.app.extension.store.Extensions;

/**
 * ExtensionConverter contains bidirectional conversions between Extension and ExtensionStore.
 *
 * @author johnniang
 */
public interface ExtensionConverter {

    /**
     * Converts Extension to ExtensionStore.
     *
     * @param extension is an Extension to be converted.
     * @param <E> is Extension type.
     * @return an ExtensionStore.
     */
    <E extends Extension> Extensions convertTo(E extension);

    /**
     * Converts Extension from ExtensionStore.
     *
     * @param type is Extension type.
     * @param extensions is an ExtensionStore
     * @param <E> is Extension type.
     * @return an Extension
     */
    <E extends Extension> E convertFrom(Class<E> type, Extensions extensions);

}
