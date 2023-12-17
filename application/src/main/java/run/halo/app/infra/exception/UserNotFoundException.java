package run.halo.app.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserNotFoundException extends ResponseStatusException {

    public UserNotFoundException(String username) {
        super(HttpStatus.NOT_FOUND, "User " + username + " was not found", null, null,
            new Object[] {username});
    }

}
