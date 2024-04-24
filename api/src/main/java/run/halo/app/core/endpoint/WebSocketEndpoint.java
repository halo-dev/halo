package run.halo.app.core.endpoint;

import org.springframework.web.reactive.socket.WebSocketHandler;
import run.halo.app.extension.GroupVersion;

/**
 * Endpoint for WebSocket.
 *
 * @author johnniang
 */
public interface WebSocketEndpoint {

    /**
     * Path of the URL after group version.
     *
     * @return path of the URL.
     */
    String urlPath();

    /**
     * Group and version parts of the endpoint.
     *
     * @return GroupVersion.
     */
    GroupVersion groupVersion();

    /**
     * Real WebSocket handler for the endpoint.
     *
     * @return WebSocket handler.
     */
    WebSocketHandler handler();

}
