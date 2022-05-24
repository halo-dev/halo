package run.halo.app.extension;

/**
 * Extension is an interface which represents an Extension. It contains setters and getters of
 * GroupVersionKind and Metadata.
 */
public interface Extension extends ExtensionOperator {

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

}
