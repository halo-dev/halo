package run.halo.app.infra.exception;

import org.springframework.web.server.ServerWebInputException;
import run.halo.app.core.extension.UserConnection;

/**
 * An exception that the user has been bound to another OAuth2 user.
 *
 * @author johnniang
 * @since 2.20.0
 */
public class OAuth2UserAlreadyBoundException extends ServerWebInputException {

    public OAuth2UserAlreadyBoundException(UserConnection connection) {
        super("The user has been bound to another account", null, null, null, new Object[] {
            connection.getSpec().getUsername(),
            connection.getSpec().getProviderUserId(),
            connection.getSpec().getRegistrationId(),
            connection.getSpec().getUpdatedAt()
        });
    }

}
