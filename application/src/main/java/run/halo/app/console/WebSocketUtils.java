package run.halo.app.console;

import java.util.Objects;
import org.springframework.http.HttpHeaders;

public enum WebSocketUtils {
    ;

    public static boolean isWebSocketUpgrade(HttpHeaders headers) {
        // See io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionUtil
        // .isWebsocketUpgrade for more.
        var upgradeConnection = headers.getConnection().stream().map(String::toLowerCase)
            .anyMatch(conn -> Objects.equals(conn, "upgrade"));

        return headers.containsKey(HttpHeaders.UPGRADE)
            && upgradeConnection
            && "websocket".equalsIgnoreCase(headers.getUpgrade());
    }

}
