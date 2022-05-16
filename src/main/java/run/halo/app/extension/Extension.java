package run.halo.app.extension;

/**
 * Extension is an interface which represents an Extension. It contains setters and getters of
 * GroupVersionKind and Metadata.
 */
public interface Extension {

    /**
     * Sets GroupVersionKind of the Extension.
     *
     * @param gvk is GroupVersionKind data.
     */
    void groupVersionKind(GroupVersionKind gvk);

    /**
     * Gets GroupVersionKind of the Extension.
     *
     * @return GroupVersionKind of the Extension.
     */
    GroupVersionKind groupVersionKind();

    /**
     * Sets metadata of the Extension.
     *
     * @param metadata metadata of the Extension.
     */
    void metadata(Metadata metadata);

    /**
     * Gets metadata of the Extension.
     *
     * @return metadata of the Extension.
     */
    Metadata metadata();

}
