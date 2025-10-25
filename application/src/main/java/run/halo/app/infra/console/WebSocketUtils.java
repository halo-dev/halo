package run.halo.app.infra.console;

import org.springframework.http.HttpHeaders;

public enum WebSocketUtils {
    ;

    public static boolean isWebSocketUpgrade(HttpHeaders headers) {
        // See io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionUtil
        // .isWebsocketUpgrade for more.
        return headers.containsHeader(HttpHeaders.UPGRADE)
            && headers.getConnection().stream().anyMatch(c -> "Upgrade".equalsIgnoreCase(c.trim()))
            && "Websocket".equalsIgnoreCase(headers.getUpgrade());
    }

}
