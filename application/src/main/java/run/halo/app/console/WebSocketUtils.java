package run.halo.app.console;

import org.springframework.http.HttpHeaders;

public enum WebSocketUtils {
    ;

    public static boolean isWebSocketUpgrade(HttpHeaders headers) {
        // See io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionUtil
        // .isWebsocketUpgrade for more.
        return headers.containsKey(HttpHeaders.UPGRADE)
            && headers.getConnection().contains(HttpHeaders.UPGRADE)
            && "websocket".equalsIgnoreCase(headers.getUpgrade());
    }

}
