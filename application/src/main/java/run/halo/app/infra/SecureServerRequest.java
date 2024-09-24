package run.halo.app.infra;

import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.support.ServerRequestWrapper;
import org.springframework.web.server.ServerWebExchange;

/**
 * Secure server request without application context available.
 *
 * @author johnniang
 * @since 2.20.0
 */
public class SecureServerRequest extends ServerRequestWrapper {

    /**
     * Create a new {@code ServerRequestWrapper} that wraps the given request.
     *
     * @param delegate the request to wrap
     */
    public SecureServerRequest(ServerRequest delegate) {
        super(delegate);
    }

    @Override
    @NonNull
    public ServerWebExchange exchange() {
        return new SecureServerWebExchange(super.exchange());
    }

}
