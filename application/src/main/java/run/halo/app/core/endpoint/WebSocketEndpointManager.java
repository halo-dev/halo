package run.halo.app.core.endpoint;

import java.util.Collection;

/**
 * Interface for managing WebSocket endpoints, including registering and unregistering.
 *
 * @author johnniang
 */
public interface WebSocketEndpointManager {

    void register(Collection<WebSocketEndpoint> endpoints);

    void unregister(Collection<WebSocketEndpoint> endpoints);

}
