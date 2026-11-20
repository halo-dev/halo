package run.halo.app.extension;

/**
 * Extension read client for public display consumers, including plugin Finders and Public APIs.
 *
 * <p>Queries and sorting operate on source data before presentation processing. Returned resources are display values
 * and must never be passed to a persistence client or shared source cache. This client implements the shared
 * {@link ReactiveExtensionReadClient} contract, while mutation and lifecycle operations remain exclusive to
 * {@link ReactiveExtensionClient}. Without an enabled public presentation context, reads retain source values.
 *
 * <pre>{@code
 * // Public Finder: the client owns processing; the Finder only builds its view.
 * return client.fetch(Tag.class, name).map(TagVo::from);
 * }</pre>
 *
 * @since 2.28.0
 */
public interface ReactivePresentationExtensionClient extends ReactiveExtensionReadClient {}
