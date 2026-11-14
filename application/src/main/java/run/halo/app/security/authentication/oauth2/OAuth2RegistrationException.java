package run.halo.app.security.authentication.oauth2;

import org.springframework.web.server.ServerWebInputException;

public class OAuth2RegistrationException extends ServerWebInputException {

    private final Error error;

    public OAuth2RegistrationException(Error error) {
        super(
                error.reason(),
                null,
                null,
                error == Error.REGISTRATION_CLOSED
                        ? "problemDetail.user.signUpFailed.disallowed"
                        : "problemDetail.user.signup.defaultRoleMissing",
                null);
        this.error = error;
    }

    public String getErrorCode() {
        return error.code();
    }

    public enum Error {
        REGISTRATION_CLOSED("registration-closed", "The registration is not allowed by the administrator."),
        DEFAULT_ROLE_MISSING("default-role-missing", "The default role is not configured by the administrator.");

        private final String code;
        private final String reason;

        Error(String code, String reason) {
            this.code = code;
            this.reason = reason;
        }

        String code() {
            return code;
        }

        String reason() {
            return reason;
        }
    }
}
